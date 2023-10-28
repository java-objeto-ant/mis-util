package org.rmj.mis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class UpdateAppUserEmail {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("UpdateAppUserEmail", "mis.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("gRider");
        
        if (!instance.logUser("gRider", "M001111122")){
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT" +
                            "  sUserIDxx" +
                            ", sEmailAdd" +
                            ", cActivatd" +
                        " FROM App_User_Master" +
                        " WHERE sProdctID = 'gRider'" +
                            " AND IFNULL(sEmployNo, '') = ''";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        ResultSet loRx;
        
        try {
            instance.beginTrans();
            while (loRS.next()){
                lsSQL = "SELECT a.sEmployID" +
                        " FROM Employee_Master001 a" +
                            ", Client_Master b" +
                        " WHERE a.sEmployID = b.sClientID" +
                            " AND b.sEmailAdd = " + SQLUtil.toSQL(loRS.getString("sEmailAdd"));
                
                loRx = instance.executeQuery(lsSQL);
                
                if (loRx.next()){
                    lsSQL = "UPDATE App_User_Master SET" +
                                "  sEmployNo = " + SQLUtil.toSQL(loRx.getString("sEmployID")) +
                                ", dActivatd = " + SQLUtil.toSQL(instance.getServerDate()) +
                                ", cActivatd = '1'" +
                            " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx"));
                    
                    if (instance.executeQuery(lsSQL, "App_User_Master", instance.getBranchCode(), "") <= 0){
                        System.err.print("Unable to update App User Master.");
                        instance.rollbackTrans();
                    }
                }
            }
        } catch (SQLException e) {
            instance.rollbackTrans();
            e.printStackTrace();
            System.exit(1);
        }
        
        instance.commitTrans();
        logwrapr.info("Thank you.");
    }
}
