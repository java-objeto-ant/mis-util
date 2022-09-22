import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rmj.mis.util.notification.SendNotification;

public class testNotification3 {
    public static void main(String [] args){
        JSONObject loRcpt = new JSONObject();
        JSONArray loRcpts = new JSONArray();
        
        loRcpt.put("app", "GuanzonApp");
        loRcpt.put("user", "GAP0190004");
        loRcpts.add(loRcpt);
        
        JSONObject loMaster = new JSONObject();
        loMaster.put("type", "00000");
        loMaster.put("parent", null);
        loMaster.put("title", "Place Order");
        loMaster.put("message", "Your order with reference number 22000001 was successfully placed.");
        loMaster.put("rcpt", loRcpts);
        
        JSONObject loInfo = new JSONObject();
        loInfo.put("sTransNox", "MX0122000001");
        
        JSONObject loDetail = new JSONObject();
        loDetail.put("module", "00002");
        loDetail.put("data", loInfo);
        
        JSONObject loJSON = new JSONObject();
        loJSON.put("master", loMaster);
        loJSON.put("detail", loDetail);
        
        String lsValue = loJSON.toJSONString();
        System.out.println(lsValue);
        
        String arg [] = {lsValue};
        
        try {
            SendNotification.main(arg);
        } catch (Exception ex) {
            Logger.getLogger(testNotification2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
