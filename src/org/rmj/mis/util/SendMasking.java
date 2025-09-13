package org.rmj.mis.util;

import java.util.ArrayList;
import org.rmj.appdriver.GRider;
import org.rmj.lib.net.LogWrapper;
import org.rmj.mis.util.sms.MaskSMS;

public class SendMasking {
    public static void main(String [] args){        
        LogWrapper logwrapr = new LogWrapper("masking", "textblast.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("IntegSys");
        
        if (!instance.logUser("IntegSys", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String message = "Missed your July payment?\n" +
                            "\n" +
                            "Pay in our office now until Aug 15 and claim your rebate.\n" +
                            "\n" +
                            "Don't miss this limited-time offer.";

        MaskSMS processor = new MaskSMS(instance, logwrapr);
        processor.setMaskName("GUANZON");
        processor.setSMS(message);
        processor.setRecipient(getRecipients());
        
        if (!processor.sendMessage()){
            logwrapr.severe(processor.getMessage());
            System.exit(1);
        }
        
        logwrapr.info("SMS sending done. Thank you.");
        System.exit(0);
    }
    
    public static ArrayList getRecipients(){
        ArrayList recipients = new ArrayList();
        recipients.add("09176340516");
        return recipients;
    }
}