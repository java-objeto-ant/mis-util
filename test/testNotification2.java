
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.mis.util.notification.SendNotification;

public class testNotification2 {
    public static void main(String [] args){
        String lsValue = "{\"detail\":{\"data\":{\"dTransact\":\"2021-11-23\",\"sBranchCD\":\"M042\",\"sTimeOpen\":\"08:30:00 AM\",\"sOpenNowx\":\"08:06:49 AM\"},\"module\":\"00002\"},\"master\":{\"rcpt\":[{\"app\":\"gRider\",\"user\":\"GAP021000064\"}],\"parent\":null,\"type\":\"00000\",\"title\":\"Branch Opening\",\"message\":\"UEMI Iloilo - Yamaha has recently opened.\"}}";
        System.out.println(lsValue);
        
        String arg [] = {lsValue};
        
        try {
            SendNotification.main(arg);
        } catch (Exception ex) {
            Logger.getLogger(testNotification2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
