package org.rmj.mis.util.json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;

public class Capture_CP_Model {
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
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
        
        JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader("D:\\Guanzon\\Reports\\MIS\\Google Play Supported Model 20190902.json"))
        {
            Object obj = jsonParser.parse(reader);
 
            JSONArray listModel = (JSONArray) obj;
            
            poGRider.beginTrans();
            
            for (int lnCtr = 0; lnCtr <= listModel.size()-1; lnCtr ++){
                JSONObject loJSON = (JSONObject) listModel.get(lnCtr);
                
                String lsBrand = loJSON.get("Retail Branding").toString();
                String lsName = loJSON.get("Marketing Name").toString();
                String lsDevice = loJSON.get("Device").toString();
                String lsModel = loJSON.get("Model").toString();
                String lsSQL = "";
                
                switch (lsBrand.toLowerCase()){
                    case "myphone":
                    case "samsung":
                    case "cherry mobile":
                    case "nokia":
                    case "lge":
                    case "sony":
                    case "lenovo":
                    case "htc":
                    case "huawei":
                    case "oppo":
                    case "asus":
                    case "cloudfone":
                    case "acer":
                    case "firefly mobile":
                    case "xiaomi":
                    case "vivo":
                        break;
                    case "infinix":
                        lsName = fixMarketName(lsBrand, lsName);       
                        lsModel = fixModelName(lsBrand, lsModel);
                        
                        lsSQL = "INSERT INTO App_User_Device_Model SET " +
                                    "  sBrandNme = " + SQLUtil.toSQL(lsBrand) +
                                    ", sMarketNm = " + SQLUtil.toSQL(lsName) +
                                    ", sDeviceNm = " + SQLUtil.toSQL(lsDevice) +
                                    ", sModelNme = " + SQLUtil.toSQL(lsModel) +
                                    ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) +
                                    ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
                }
                
                if (!lsSQL.equals("")){
                    if (poGRider.executeQuery(lsSQL, "App_User_Device_Model", "", "") <= 0){
                        System.err.println(poGRider.getErrMsg() + "; " + poGRider.getMessage());
                        poGRider.rollbackTrans();
                        System.exit(1);
                    }
                }
            }
            poGRider.commitTrans();
        } catch (FileNotFoundException e) {
            poGRider.rollbackTrans();
            e.printStackTrace();
        } catch (IOException | ParseException e) {
            poGRider.rollbackTrans();
            e.printStackTrace();
        }   
    }
    
    private static String fixMarketName(String fsBrand, String fsValue){
        if (fsValue.isEmpty()) return "";
        
        //remove \t from text
        if (fsValue.contains("\\t")){
            fsValue =  fsValue.replace("\\t", "");
        }
        
        //remove string values with \ prefix
        if (fsValue.contains("\\")){                          
            String [] laName = fsValue.split(" ");

            fsValue = "";
            for (int lnChar = 0; lnChar <= laName.length -1; lnChar ++){
                if (!laName[lnChar].contains("\\")){
                    fsValue = fsValue + " " + laName[lnChar];
                }
            }
        }
        
        //remove brand names from the market names
        if (fsValue.contains(fsBrand)){
            fsValue = fsValue.replace(fsBrand, "");
        }
        
        return fsValue.trim();
    }
    
    private static String fixModelName(String fsBrand, String fsValue){
        if (fsValue.isEmpty()) return "";
         
        //remove brand names from the market names
        if (fsValue.contains(fsBrand)){
            fsValue = fsValue.replace(fsBrand, "");
        }
        
        return fsValue.trim();
    }
    
}
