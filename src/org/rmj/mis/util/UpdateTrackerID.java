package org.rmj.mis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class UpdateTrackerID {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("UpdateTrackerID", "mis.log");
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
                            "  a.sTransNox" +
                            ", a.sTrackrID" +
                            ", b.sTrackrID xTrackrID" +
                        " FROM Support_Request_Master a" +
                                ", Tracker_Monitor b" +
                        " WHERE a.sTransNox = b.sTransNox" +
                            " AND a.sTrackrID = '315DDX000770'" +
                            " AND a.sTrackrID <> b.sTrackrID";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){
                lsSQL = "UPDATE Support_Request_Master SET" +
                            "  sTrackrID = " + SQLUtil.toSQL(loRS.getString("xTrackrID")) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                
                instance.beginTrans();
                if (instance.executeQuery(lsSQL, "Support_Request_Master", loRS.getString("sTransNox").substring(0, 4), "") <= 0){
                    instance.rollbackTrans();
                    System.err.println(instance.getErrMsg() + "; " + instance.getMessage());
                    System.exit(1);
                }
                instance.commitTrans();
            }
        } catch (SQLException ex) {
            instance.rollbackTrans();
            ex.printStackTrace();
            System.exit(1);
        }
        
        logwrapr.info("Thank you.");
        System.exit(0);
    }
}
