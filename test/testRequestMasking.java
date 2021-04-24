

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.WebClient;

public class testRequestMasking {   
    public static void main (String [] args){   
        //if using default Mask Name(GUANZON)
        if (sendMessage("GUANZON", "09260375777", "Testing March 15"))
            System.out.println("Message sent successfuly.");
        else
            System.err.println("Unable to send message.");
        
        //if using LOSPEDRITOS as Mask Name
        if (sendMessage("LOSPEDRITOS", "09260375777", "Testing March 15"))
            System.out.println("Message sent successfuly.");
        else
           System.err.println("Unable to send message.");
        
        //if using CARTRADE as Mask Name
        if (sendMessage("CARTRADE", "09260375777", "Testing March 15"))
            System.out.println("Message sent successfuly.");
        else
            System.err.println("Unable to send message.");
        
        //NOTE:
        //  Logging of message was not done on API,
        //  Please make your own saving of entries for history.
    }
    
    private static boolean sendMessage(String fsMaskName, String fsMobileNo, String fsMessage){        
        String fsURL = "https://restgk.guanzongroup.com.ph/system/masking/sendSMS.php";
        
        String clientid = "GGC_BM001"; //this must be replaced based on the client id using it
        String productid = "IntegSys"; //this must be replaced based on the product id using it
        String imei = "GMC_SEG09"; //this must be replaced based on the computer name using it
        String userid = "M001111122"; //this must be replaced based on the user id using it
        
        Calendar calendar = Calendar.getInstance();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("g-api-id", productid);
        headers.put("g-api-imei", imei);
        
        headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));        
        headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
        headers.put("g-api-client", clientid);    
        headers.put("g-api-user", userid);    
        headers.put("g-api-log", "");    
        headers.put("g-api-token", "");    
        headers.put("g-api-mobile", "");    
        
        JSONObject param = new JSONObject();
        param.put("message", fsMessage);
        param.put("mobileno", fsMobileNo);
        param.put("maskname", fsMaskName);
        
        String response;
        try {
            response = WebClient.sendHTTP(fsURL, param.toJSONString(), (HashMap<String, String>) headers);
            if(response == null){
                System.err.println("No Response");
                return false;
            } 

            JSONParser loParser = new JSONParser();
            JSONObject loJSON = (JSONObject) loParser.parse(response);
            
            if (loJSON.get("result").equals("success")){
                System.out.println((String) loJSON.get("message") + "(" + (String) loJSON.get("maskname") + " - " + (String) loJSON.get("id") + ")");
                return true;
            } else {
                loJSON = (JSONObject) loJSON.get("error");
                System.err.println((String) loJSON.get("code") + " - " + (String) loJSON.get("message"));
                return false;
            }
            
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
