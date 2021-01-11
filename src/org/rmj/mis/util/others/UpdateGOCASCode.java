package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class UpdateGOCASCode {
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
        
        String lsSQL = "SELECT * FROM xxxSysConfig";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        
        try {
            poGRider.beginTrans();
            while (loRS.next()){
                lsSQL = "UPDATE xxxSysConfig SET sConfigCd = " + SQLUtil.toSQL(loRS.getString("sConfigCd").trim()) + " WHERE sConfigCd = " + SQLUtil.toSQL(loRS.getString("sConfigCd"));
                
                poGRider.executeQuery(lsSQL, "xxxSysConfig", "M001", "");
            }
            poGRider.commitTrans();
        } catch (SQLException ex) {
            poGRider.rollbackTrans();
            Logger.getLogger(UpdateGOCASCode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
