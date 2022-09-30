package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.factory.UtilityValidator;
import org.rmj.mis.util.factory.UtilityValidatorFactory;
import org.rmj.mis.util.raffle.RaffleValidator;
import org.rmj.mis.util.raffle.RaffleValidatorFactory;
import org.rmj.replication.utility.LogWrapper;

public class CreateRaffle2022 {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("CreateRaffle2022", "raffle.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("IntegSys");
        
        if (!instance.logUser("IntegSys", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        RaffleValidator utility;
        
        //process lr payment
        utility = RaffleValidatorFactory.make(RaffleValidatorFactory.UtilityType.MOTORCYCLE_SALES);
        utility.setGRider(instance);
        utility.setBranch("M001");
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        } 
        
        //process mobile phone sales
        utility = RaffleValidatorFactory.make(RaffleValidatorFactory.UtilityType.MOBILE_PHONE_SALES);
        utility.setGRider(instance);
        utility.setBranch("C001");
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        } 
        
        //process lr payment
        utility = RaffleValidatorFactory.make(RaffleValidatorFactory.UtilityType.LR_PAYMENT);
        utility.setGRider(instance);
        utility.setBranch("C001");
        
        if (!utility.Run()){
            System.err.println(utility.getMessage());
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        } 
       
        System.out.println("Thank you!");
        //return success
        System.exit(0); 
    }
}
