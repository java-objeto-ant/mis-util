package org.rmj.mis.util;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.lr.Create3CLeads;
import org.rmj.replication.utility.LogWrapper;

public class Create3C {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("3C", "recalc.log");
        
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
        
        Create3CLeads create = new Create3CLeads(instance);
        
        create.setDateFrom("2026-01-03");
        create.setDateThru("2026-02-02");
        
        System.out.println(instance.getBranchCode());
        if (!create.Create()){
            logwrapr.severe(create.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}