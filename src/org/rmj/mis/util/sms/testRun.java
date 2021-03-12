package org.rmj.mis.util.sms;

import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class testRun {
    public static void main(String [] args){        
        LogWrapper logwrapr = new LogWrapper("remP", "css.log");
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
        
        iSMS processor = new FUPYMT();
        processor.setGRider(instance);
        if (!processor.Process()){
            logwrapr.severe(processor.getMessage());
            System.exit(1);
        }
            
        logwrapr.info(processor.getItemCount() + " messages was created for " + SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE) + 
                        " with " + processor.getInvalid() + " invalid mobile numbers.");
        System.exit(0);
    }
}
