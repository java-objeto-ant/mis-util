package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

class UpdateSPSuperceded {
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
        final String USERID = "M001111122";
        
        GRider poGRider = new GRider(PRODUCTID);

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            MiscUtil.close(poGRider.getConnection());
            System.exit(1);
        }
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            MiscUtil.close(poGRider.getConnection());
            System.exit(1);
        }
        
        Supersesion instance = new Supersesion(poGRider, poGRider.getBranchCode(), false);
        instance.ReAlignSupersede();
    }
}

class Supersesion{
    GRider oApp;
    String sBranchCd;
    boolean bWithParent;
    
    String sMessage;
    
    private void setMessage(String fsValue){
        sMessage = fsValue;
    }
    public String getMessage(){
        return sMessage;
    }
    
    public Supersesion(GRider foApp, String fsBranchCd, boolean fbWithParent){
        oApp = foApp;
        sBranchCd = fsBranchCd;
        bWithParent = fbWithParent;
    }
    
    public boolean ReAlignSupersede(){
        if (oApp == null){
            setMessage("Application driver is not set...");
            return false;
        }
        
        String lsSQL = "SELECT" + 
                            "  sPartsIDx" + 
                            ", sBarrCode" + 
                            ", sReplacID" + 
                        " FROM Spareparts" + 
                        " WHERE IFNULL(sReplacID, '') <> ''" +
                            " AND (`sReplacID` NOT LIKE 'M0%' AND `sReplacID` NOT LIKE 'GC%')";
        
        ResultSet loRS = oApp.executeQuery(lsSQL);
        ResultSet loRSx;
        
        try {
            while (loRS.next()){
                lsSQL = "SELECT" +
                            "  sPartsIDx" +
                        " FROM Spareparts" + 
                        " WHERE sBarrCode = " + SQLUtil.toSQL(loRS.getString("sReplacID"));
                
                loRSx = oApp.executeQuery(lsSQL);
                
                if (MiscUtil.RecordCount(loRSx) == 1){
                    loRSx.first();
                    
                    lsSQL = "UPDATE Spareparts SET" + 
                                "  sReplacID = " + SQLUtil.toSQL(loRSx.getString("sPartsIDx")) + 
                                ", sModified = " + SQLUtil.toSQL(oApp.getUserID()) + 
                                ", dModified = " + SQLUtil.toSQL(oApp.getServerDate()) + 
                            " WHERE sPartsIDx = " + SQLUtil.toSQL(loRS.getString("sPartsIDx"));
                    
                    oApp.executeQuery(lsSQL, "Spareparts", sBranchCd, "");
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(Supersesion.class.getName()).log(Level.SEVERE, null, ex);
            setMessage(ex.getMessage());
            return false;
        }
        
        setMessage("Records successfully updated;");
        
        return true;
    }
}
