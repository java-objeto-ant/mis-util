package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.factory.UtilityValidator;
import org.rmj.mis.util.factory.UtilityValidatorFactory;
import org.rmj.replication.utility.LogWrapper;

public class CreateSource {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("CreateSource", "mis.log");
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
        
        //process applicant occupation discrepancy
        utility = UtilityValidatorFactory.make(UtilityValidatorFactory.UtilityType.MONITORING_BOARD);
        utility.setGRider(instance);
        
        if (!utility.Run()){
            logwrapr.severe(utility.getMessage());
            System.exit(1);
        }
        
        System.out.println("Thanks!!!");
        System.exit(0);
    }
}
