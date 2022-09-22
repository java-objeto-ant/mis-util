package org.rmj.mis.util.notification;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.rmj.appdriver.SQLUtil;
import org.rmj.lib.net.WebClient;
import org.rmj.replication.utility.LogWrapper;

public class SendNotification {
    public static void main(String[] args) throws Exception{
        LogWrapper logwrapr = new LogWrapper("SendNotification", "branch-opening.log");
        logwrapr.info("Start of Process!");
        
        String sURL = "https://restgk.guanzongroup.com.ph/notification/send_request.php";   
        
        Calendar calendar = Calendar.getInstance();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("g-api-id", "GuanzonApp");
        headers.put("g-api-imei", "356060072281722");
        headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));    
        headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));    
        headers.put("g-api-user", "GAP0190001");   
        headers.put("g-api-mobile", "09171870011");
        headers.put("g-api-token", "cPYKpB-pPYM:APA91bE82C4lKZduL9B2WA1Ygd0znWEUl9rM7pflSlpYLQJq4Nl9l5W4tWinyy5RCLNTSs3bX3JjOVhYnmCpe7zM98cENXt5tIHwW_2P8Q3BXI7gYtEMTJN5JxirOjNTzxWHkWDEafza");    

        if (args.length < 1) {
            logwrapr.severe("No argument detected.");
            System.exit(1); 
        }
        
        String lsJSON = args[0];
        
        JSONParser loParser = new JSONParser();
        JSONObject loJSON = (JSONObject) loParser.parse(lsJSON);
        
        JSONObject param = (JSONObject) loJSON.get("master");
        param.put("infox", (JSONObject) loJSON.get("detail"));

        String response = WebClient.httpsPostJSon(sURL, param.toJSONString(), (HashMap<String, String>) headers);
        if(response == null){
            logwrapr.severe("HTTP Error detected: " + System.getProperty("store.error.info"));
            System.exit(1);
        }
        
        loJSON = (JSONObject) loParser.parse(response);
        if (!loJSON.get("result").equals("success")){
            loJSON = (JSONObject) loJSON.get("error");
            logwrapr.severe((String) loJSON.get("message"));
            System.exit(1);
        }
        System.out.println(response);
        System.exit(0);
    }
}