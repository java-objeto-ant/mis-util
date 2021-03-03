package org.rmj.mis.util.gapp;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.WebClient;

public class SendEmailVerification {
    final static String SEND_VERIFICATION = "security/send-verification.php";
    
    public static void main(String [] args){
        final String PRODUCTID = "IntegSys";
        final String USERID = "M001111122";
        
        GRider poGRider = new GRider(PRODUCTID);

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
                
        String lsSQL = "SELECT * FROM App_User_Master" +
                        " WHERE sProdctID = 'GuanzonApp'" +
                            " AND cActivatd = '0'" + 
                            " AND sUserIDxx IN ('GAP020201647')";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            JSONObject param = new JSONObject();
            while (loRS.next()){
                param.clear();
                param.put("email", loRS.getString("sEmailAdd"));
                param.put("username", loRS.getString("sUserName"));
                param.put("password", loRS.getString("sPassword"));
                param.put("hash", loRS.getString("sItIsASIN"));
                
                sendEmail(poGRider, param);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
    
    private static void sendEmail(GRider foGRider, JSONObject loJSON){
        Calendar calendar = Calendar.getInstance();
        
        Map<String, String> headers =
                new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("g-api-id", "GuanzonApp");
        headers.put("g-api-imei", MiscUtil.getPCName());
        headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));
        headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
        headers.put("g-api-client", foGRider.getClientID());
        headers.put("g-api-user", foGRider.getUserID());
        headers.put("g-api-log", "");
        headers.put("g-api-token", "");
        
        JSONObject param = new JSONObject();
        param.put("email", loJSON.get("email"));
        param.put("username", loJSON.get("username"));
        param.put("password", loJSON.get("password"));
        param.put("hash", loJSON.get("hash"));
        
        JSONParser oParser = new JSONParser();
        JSONObject json_obj = null;
        
        try {
            System.out.println(CommonUtils.getConfiguration(foGRider, "WebSvr") + SEND_VERIFICATION);
            String response = WebClient.sendHTTP(CommonUtils.getConfiguration(foGRider, "WebSvr") + SEND_VERIFICATION, param.toJSONString(), (HashMap<String, String>) headers);
            if(response == null){
                System.err.println("No response from the server. ->>" + SEND_VERIFICATION);
                System.exit(1);
            }
            json_obj = (JSONObject) oParser.parse(response);
            response = (String) json_obj.get("result");
            
            if (response.equalsIgnoreCase("success")){
                System.out.println((String) json_obj.get("message") + " ->> " + (String) loJSON.get("email"));
            } else {
                System.err.println((String) json_obj.get("message"));
                System.exit(1);
            }
        } catch (IOException | ParseException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
