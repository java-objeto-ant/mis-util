package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.lr.Create3CLeads;
import org.rmj.mis.util.lr.RecalculateAR;
import org.rmj.replication.utility.LogWrapper;

public class Recalculate {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("Recalculate", "recalc.log");
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
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        RecalculateAR recalc = new RecalculateAR(instance);
        
        if (!recalc.Recalc()){
            logwrapr.severe(recalc.getMessage());
            System.exit(1);
        }

        logwrapr.info("End of Process!");
        System.exit(0);
    }
}
