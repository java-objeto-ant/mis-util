package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.factory.UtilityValidator;
import org.rmj.mis.util.factory.UtilityValidatorFactory;
import org.rmj.replication.utility.LogWrapper;

public class TLMAccounts {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("TLM.Utility", "tlm.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("TeleMktg");
        
        if (!instance.logUser("TeleMktg", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        UtilityValidator utility;
        
        //fix no subscriber inquiries
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.TLM_FIX);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }
        
        //process benta
//        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.GANADO);
//        utility.setGRider(instance);
//        
//        if (!utility.Run()){
//            System.err.println(utility.getMessage());
//            logwrapr.severe(utility.getMessage());
//            System.exit(1);
//        }
        
        //process TLM Primary Leads
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.TLM_PRIMARY_LEADS);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        } 
        
        //process TLM Leads from approved credit applications
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.TLM_CA_LEADS);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }
        
        //process TLM Leads from MC Sales
//        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.TLM_MCSO_AS_MP_LEADS);
//        utility.setGRider(instance);
//        
//        if (!utility.Run()){
//            System.err.println(utility.getMessage());
//            logwrapr.severe(utility.getMessage());
//            System.exit(1);
//        } 
        
        //classify client mobile
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