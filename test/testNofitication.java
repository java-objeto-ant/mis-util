
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.rmj.appdriver.SQLUtil;
import org.rmj.lib.net.WebClient;

public class testNofitication {
    public static void main(String [] args){
        String sURL = "https://restgk.guanzongroup.com.ph/notification/send_request.php";        
        Calendar calendar = Calendar.getInstance();
        //Create the header section needed by the API
        Map<String, String> headers = 
                        new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("g-api-id", "IntegSys");
        headers.put("g-api-imei", "0bc6b726d347470e");
        headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));    
        headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));    
        headers.put("g-api-user", "GAP0190004");   
        headers.put("g-api-mobile", "09260375777");
        headers.put("g-api-token", "0bc6b726d347470e");    
        headers.put("g-api-log", "GAP021017201");

        JSONArray rcpts = new JSONArray();
        JSONObject rcpt = new JSONObject();
        rcpt.put("app", "gRider");
        rcpt.put("user", "GAP0190004");
        rcpts.add(rcpt);

        //Create the parameters needed by the API
        JSONObject param = new JSONObject();
        param.put("type", "00000");
        param.put("parent", null);
        param.put("title", "Testing Header 20210512");
        param.put("message", "Testing Body 20210512.");
        param.put("rcpt", rcpts);

        JSONParser oParser = new JSONParser();
        JSONObject json_obj = null;

        String response;
        try {
            response = WebClient.httpsPostJSon(sURL, param.toJSONString(), (HashMap<String, String>) headers);
            
            if(response == null){
                System.out.println("HTTP Error detected: " + System.getProperty("store.error.info"));
                System.exit(1);
            }
            
            System.out.println(response);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }
}
