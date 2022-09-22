package org.rmj.mis.util.notification;

import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class BranchOpeningUtil {
    public static void main(String [] args){        
        LogWrapper logwrapr = new LogWrapper("BranchOpeningUtil", "branch-opening.log");
        
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
        
        BranchOpening processor = new BranchOpening();        
        processor.setGRider(instance);
        
        if (args.length == 1) processor.setBranchCode(args[0]);
        
        if (!processor.Run()){
            logwrapr.severe(processor.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
