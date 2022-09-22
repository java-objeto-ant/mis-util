package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.factory.UtilityValidator;
import org.rmj.mis.util.factory.UtilityValidatorFactory;
import org.rmj.replication.utility.LogWrapper;

public class MobileClassify {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("MobileClassify", "tlm.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("TeleMktg");
        
        if (!instance.logUser("TeleMktg", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        UtilityValidator utility;
        
        //process TLM Primary Leads
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.CLASSIFY_MOBILE);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }         
        
        System.out.println("Thank you!");
        logwrapr.info("Thank you!");
        //return success
        System.exit(0); 
    }
}
