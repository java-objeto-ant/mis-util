package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.hcm.Absenteeism;
import org.rmj.replication.utility.LogWrapper;

public class NotifyAbsenteeism {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("NotifyAbsenteeism", "hcm.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("PETMgr");
        
        if (!instance.logUser("PETMgr", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        Absenteeism utility = new Absenteeism();
        utility.setGRider(instance);
        if (utility.Run()){
            System.exit(0);
        } else {
            System.err.println(utility.getMessage());
            System.exit(1);
        }
    }
}
