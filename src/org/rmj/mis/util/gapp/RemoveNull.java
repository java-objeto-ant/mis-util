package org.rmj.mis.util.gapp;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class RemoveNull {
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
        final String USERID = "M001111122";
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX poGRider = new GRiderX(PRODUCTID);

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }

        try {
            String lsSQL = "Select * FROM App_User_Master WHERE `sUserName` like '%null'";
        
            ResultSet loRS = poGRider.executeQuery(lsSQL);

            while (loRS.next()){
                lsSQL = loRS.getString("sUserName");
                lsSQL = lsSQL.replace(" null", "");
                
                lsSQL = "UPDATE App_User_Master SET" +
                            "  sUserName = " + SQLUtil.toSQL(lsSQL) +
                        " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx"));
                System.out.println(lsSQL);
                poGRider.executeUpdate(lsSQL);
            }
            
            System.exit(0);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
