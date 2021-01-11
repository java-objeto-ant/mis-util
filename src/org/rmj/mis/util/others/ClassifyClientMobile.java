package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;

public class ClassifyClientMobile {
    public static void main(String [] args){
        final String PRODUCTID = "IntegSys";
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
        
        //String lsSQL = "SELECT * FROM Client_Mobile WHERE IFNULL(cSubscrbr, '') = '' AND LENGTH(sMobileNo) >= 11 AND cRecdStat = '1' LIMIT 25000";
        
        String lsSQL = "SELECT a.sAcctNmbr, b.sClientID, b.nEntryNox, b.sMobileNo, b.cSubscrbr, b.cRecdStat" +
                        " FROM MC_AR_Master a" +
                            ", Client_Mobile b" +
                        " WHERE a.sClientID = b.sClientID" +
                            " AND a.cAcctstat = '0'" +
                            " AND IFNULL(a.cActTypex, '0') = '0'" +
                            " AND IFNULL(b.cSubscrbr, '') = ''" +
                            " AND LENGTH(b.sMobileNo) = 11" +
                            " AND LEFT(b.sMobileNo, 2) = '09'" +
                        " ORDER BY b.sClientID, b.nEntryNox";

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        System.out.println("ITEM COUNT: " + MiscUtil.RecordCount(loRS));
        
        try {
            String lcSubscrbr = "";
            
            while (loRS.next()){
                lcSubscrbr = CommonUtils.classifyNetwork(loRS.getString("sMobileNo"));
                
                if (!lcSubscrbr.isEmpty()){
                    poGRider.beginTrans();
                    
                    lsSQL= "UPDATE Client_Mobile SET" +
                                "  cSubscrbr = " + SQLUtil.toSQL(lcSubscrbr) +
                                ", cRecdStat = '1'" +
                            " WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                                " AND sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo"));
                    
                    if (poGRider.executeQuery(lsSQL, "Client_Mobile", poGRider.getBranchCode(), "") <= 0){
                        if (!poGRider.getErrMsg().isEmpty()){
                            poGRider.rollbackTrans();
                            System.err.println(poGRider.getErrMsg());
                            System.exit(1);
                        }
                    }
                    
                    poGRider.commitTrans();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClassifyClientMobile.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        lsSQL = "SELECT a.sAcctNmbr, b.sClientID, b.nEntryNox, b.sMobileNo, b.cSubscrbr, b.cRecdStat" +
                        " FROM MC_AR_Master a" +
                            ", Client_Mobile b" +
                        " WHERE a.sClientID = b.sClientID" +
                            " AND a.cAcctstat = '0'" +
                            " AND IFNULL(a.cActTypex, '0') = '0'" +
                            " AND IFNULL(b.cSubscrbr, '') <> ''" +
                            " AND LENGTH(b.sMobileNo) = 11" +
                            " AND LEFT(b.sMobileNo, 2) = '09'" +
                            " AND IFNULL(b.cRecdStat, '0') = '0'" +
                        " ORDER BY b.sClientID, b.nEntryNox";

        loRS = poGRider.executeQuery(lsSQL);
        
        System.out.println("ITEM COUNT: " + MiscUtil.RecordCount(loRS));
        
        try {
            String lcSubscrbr = "";
            
            while (loRS.next()){
                lcSubscrbr = CommonUtils.classifyNetwork(loRS.getString("sMobileNo"));
                
                if (!lcSubscrbr.isEmpty()){
                    poGRider.beginTrans();
                    
                    lsSQL= "UPDATE Client_Mobile SET" +
                                "  cRecdStat = '1'" +
                            " WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                                " AND sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo"));
                    
                    if (poGRider.executeQuery(lsSQL, "Client_Mobile", poGRider.getBranchCode(), "") <= 0){
                        if (!poGRider.getErrMsg().isEmpty()){
                            poGRider.rollbackTrans();
                            System.err.println(poGRider.getErrMsg());
                            System.exit(1);
                        }
                    }
                    
                    poGRider.commitTrans();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClassifyClientMobile.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        System.exit(0);
    }
}
