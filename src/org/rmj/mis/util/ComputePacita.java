package org.rmj.mis.util;

import java.sql.ResultSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.lr.Create3CLeads;
import org.rmj.replication.utility.LogWrapper;

public class ComputePacita {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("ComputePacita", "pacita.log");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("gRider");
        
        if(!instance.getErrMsg().isEmpty()){
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT * FROM Pacita_Evaluation WHERE nRatingxx <= 0.00";
        ResultSet loRS = instance.executeQuery(lsSQL);
       
        try {
            JSONParser loParser = new JSONParser();
            JSONObject loJSON;
            JSONArray loArr;
            
            String lsDeptIDx;
            
            double lnScore;
            ResultSet loRx;
            
            while (loRS.next()){
                lnScore = 0.00;
                
                loJSON = (JSONObject) loParser.parse(loRS.getString("sPayloadx"));
                
                System.out.println(loRS.getString("sTransNox"));
                lsDeptIDx = (String) loJSON.get("sEvalType");
                
                switch (lsDeptIDx) {
                    case "015":
                        lsDeptIDx = "4"; break;
                    //guest officers
                    case "023": //Facility & Security Management(Alyana) 
                    case "025": //Marketing
                        lsDeptIDx = "026"; break;
                        
                }
                
                loArr = (JSONArray) loJSON.get("sPayloadx");
                
                for (int lnCtr = 0; lnCtr <= loArr.size()-1; lnCtr++){
                    loJSON = (JSONObject)loArr.get(lnCtr);
                    
                    if ("1".equals((String) loJSON.get("xRatingxx"))){
                        lsSQL = "SELECT nMaxValue" +
                                " FROM Pacita_Rule" +
                                " WHERE sEvalType = " + SQLUtil.toSQL(lsDeptIDx) +
                                    " AND nEntryNox = " + (int) (long) loJSON.get("nEntryNox");
                    
                        loRx = instance.executeQuery(lsSQL);
                        
                        if (loRx.next()){
                            lnScore += loRx.getDouble("nMaxValue");
                        }
                    }
                }
                
                if (lnScore > 0){
                    lsSQL = "UPDATE Pacita_Evaluation SET" +
                                "  nRatingxx = " + lnScore +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    
                    if (instance.executeQuery(lsSQL, "Pacita_Evaluation", instance.getBranchCode(), "") <= 0){
                        logwrapr.severe(instance.getErrMsg() + "; " + instance.getMessage());
                        System.exit(1);
                    }
                }
            }
        } catch (Exception e) {
            logwrapr.severe(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
    }
}
