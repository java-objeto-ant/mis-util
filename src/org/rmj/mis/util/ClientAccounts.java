package org.rmj.mis.util;

import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.factory.UtilityValidator;
import org.rmj.mis.util.factory.UtilityValidatorFactory;
import org.rmj.replication.utility.LogWrapper;

public class ClientAccounts {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("Client.Utility", "clients.log");
        logwrapr.info("Start of Process!");
        
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
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        UtilityValidator utility;
        
        //process API Payments
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.API_PAYMENTS);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }
        
        //process Client Mobile
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.CLIENT_MOBILE);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }
        
        //proccess App Users requesting for account deactivation
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.GCONNECT);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }
        
        
        //close connection
        instance.logoutUser();
        MiscUtil.close(instance.getConnection());
        
        
        System.out.println("Thank you!");
        logwrapr.info("Thank you!");
        //return success
        System.exit(0); 
    }
}
