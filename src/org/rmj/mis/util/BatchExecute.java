package org.rmj.mis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class BatchExecute {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("BatchExecute", "mis.log");
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
        
        JSONArray loStatement = getStatement();
        
        if (loStatement.isEmpty()) {
            logwrapr.severe("No statment to execute.");
            System.exit(1);
        }
        
        instance.beginTrans();
        for (int lnCtr = 0; lnCtr <= loStatement.size()-1; lnCtr++){
            instance.executeQuery((String) loStatement.get(lnCtr), "Employee_Shift", instance.getBranchCode(), "");
//            if (instance.executeQuery((String) loStatement.get(lnCtr), "", instance.getBranchCode(), "") <= 0){
//                instance.rollbackTrans();
//                logwrapr.severe(instance.getMessage() + instance.getErrMsg());
//                System.exit(1);
//            }
        }
        instance.commitTrans();
        logwrapr.info("Thank you.");
    }
    
    public static JSONArray getStatement(){
        JSONArray loArray = new JSONArray();
        
        try {
            File myObj = new File("for-execute.txt");
            Scanner myReader = new Scanner(myObj);
            
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                loArray.add(data);
            }
            
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
        System.err.println(loArray.toJSONString());
        return loArray;
    }
}
