package org.rmj.mis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class FixGOCASModel {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("FixGOCASModel", "mis.log");
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
        
        String lsSQL = "SELECT sTransNox, sReferNox" +
                        " FROM MC_Credit_Application" +
                        " WHERE IFNULL(sReferNox, '') <> ''" +
                            " AND sModelIDx = ''";


        ResultSet loRS = instance.executeQuery(lsSQL);
        boolean lbHasRecord = MiscUtil.RecordCount(loRS) > 0;
        
        try {
            if (lbHasRecord) instance.beginTrans();

            ResultSet loRx;
            JSONObject loJSON;
            JSONParser loParser = new JSONParser();
            
            while (loRS.next()){
                lsSQL = "SELECT IFNULL(sCatInfox, sDetlInfo) sCatInfox FROM Credit_Online_Application WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sReferNox"));
            
                loRx = instance.executeQuery(lsSQL);
                
                if (loRx.next()){
                    loJSON = (JSONObject) loParser.parse(loRx.getString("sCatInfox"));
                    
                    lsSQL = (String) loJSON.get("sModelIDx");
                    
                    if (!lsSQL.isEmpty()){
                        lsSQL = "UPDATE MC_Credit_Application SET" +
                                    "  sModelIDx = " + SQLUtil.toSQL(lsSQL) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                        
                        if (instance.executeQuery(lsSQL, "MC_Credit_Application", instance.getBranchCode(), "") <= 0){
                            if (lbHasRecord) instance.rollbackTrans();
                            System.err.println(lsSQL);
                            System.exit(1);
                        }
                    }
                }
            }
            
            if (lbHasRecord) instance.commitTrans();
        } catch (SQLException | ParseException e) {
            if (lbHasRecord) instance.rollbackTrans();
            e.printStackTrace();
        }
        logwrapr.info("Thank you.");
    }
}
