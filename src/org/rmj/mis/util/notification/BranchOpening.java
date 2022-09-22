package org.rmj.mis.util.notification;

import java.io.IOException;
import org.rmj.mis.util.factory.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.lib.net.WebClient;

public class BranchOpening implements UtilityValidator{    
    private GRiderX instance;
    private String sMessage;
    private String sBranchCd;
    
    @Override
    public void setGRider(GRiderX foValue) {
        instance = foValue;
        
        sBranchCd = "";
        sMessage = "";
        
        if (instance == null){
            System.err.println("Application driver is not set.");
            System.exit(1);
        }
    }
    
    public void setBranchCode(String fsValue){
        sBranchCd = fsValue;
    }

    @Override
    public boolean Run() {
        try {
            String lsSQL;
            ResultSet loMaster;
            ResultSet loArea;
            
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.dTransact = " + SQLUtil.toSQL(SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)));
            
            if (!sBranchCd.isEmpty()) lsSQL = MiscUtil.addCondition(lsSQL, "b.sBranchCd = " + SQLUtil.toSQL(sBranchCd));
            
            loMaster = instance.executeQuery(lsSQL);
            
            JSONObject loJSON;
            JSONObject loInfo;
            JSONObject loMain;
            JSONObject loRcpt;
            JSONObject loData;
            JSONArray loRcpts;            
                        
            while (loMaster.next()){
                lsSQL = MiscUtil.addCondition(getSQ_AH(), "a.sAreaCode = " + SQLUtil.toSQL(loMaster.getString("sAreaCode")));
                loArea = instance.executeQuery(lsSQL);
                
                if (loArea.next()){
                    if ("gRider".equals(loArea.getString("sProdctID"))){
                        loRcpts = new JSONArray();
                        loRcpt = new JSONObject();
                        loRcpt.put("app", "gRider");
                        loRcpt.put("user", loArea.getString("sUserIDxx"));
                        loRcpts.add(loRcpt);

                        loMain = new JSONObject();
                        loMain.put("type", "00000");
                        loMain.put("parent", null);
                        loMain.put("title", "Branch Opening");
                        loMain.put("message", loMaster.getString("sBranchNm") + " has opened.");
                        loMain.put("rcpt", loRcpts);

                        loData = new JSONObject();
                        loData.put("module", "00002");
                        
                        loInfo = new JSONObject();
                        loInfo.put("sBranchCD", loMaster.getString("sBranchCd"));
                        loInfo.put("dTransact", loMaster.getString("dTransact"));
                        
                        if (Integer.parseInt(loMaster.getString("sTimeOpen").substring(0, 2)) > 12)
                            loInfo.put("sTimeOpen", loMaster.getString("sTimeOpen") + " PM");
                        else
                            loInfo.put("sTimeOpen", loMaster.getString("sTimeOpen") + " AM");
                         
                        if (Integer.parseInt(loMaster.getString("sOpenNowx").substring(0, 2)) > 12)
                            loInfo.put("sOpenNowx", loMaster.getString("sOpenNowx") + " PM");
                        else
                            loInfo.put("sOpenNowx", loMaster.getString("sOpenNowx") + " AM");    
                            
                        loData.put("data", loInfo);
                        
                        loJSON = new JSONObject();
                        loJSON.put("master", loMain);
                        loJSON.put("detail", loData);
                        
                        System.out.println(loJSON.toJSONString());
                        if (sendNotification(loJSON.toJSONString())){
                            lsSQL = "UPDATE Branch_Opening SET" +
                                        "  dNotified = " + SQLUtil.toSQL(instance.getServerDate()) +
                                    " WHERE sBranchCd = " + SQLUtil.toSQL(loMaster.getString("sBranchCd")) +
                                        " AND dTransact = " + SQLUtil.toSQL(loMaster.getString("dTransact"));
                            
                            instance.executeUpdate(lsSQL);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage("SQL Exception...");
            return false;
        } catch (Exception ex1){
            ex1.printStackTrace();
            setMessage("Exception...");
            return false;
        }
        
        System.out.println("Utility processing done.");        
        return true;
    }

    @Override
    public void setMessage(String fsValue) {
        sMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return sMessage;
    }
    
    private boolean sendNotification(String lsValue){
        try {
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

            JSONParser loParser = new JSONParser();
            JSONObject loJSON = (JSONObject) loParser.parse(lsValue);

            JSONObject param = (JSONObject) loJSON.get("master");
            param.put("infox", loJSON.get("detail").toString());
            
            String response = WebClient.httpsPostJSon(sURL, param.toJSONString(), (HashMap<String, String>) headers);
            
            if(response == null){
                setMessage("HTTP Error detected: " + System.getProperty("store.error.info"));
                return false;
            }
            
            loJSON = (JSONObject) loParser.parse(response);
            if (!loJSON.get("result").equals("success")){
                loJSON = (JSONObject) loJSON.get("error");
                setMessage((String) loJSON.get("message"));
                return false;
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            setMessage("Exception...");
            return false;
        }
        
        return true;
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  b.sAreaCode" +
                    ", a.sBranchCd" +
                    ", a.dTransact" + 
                    ", a.sTimeOpen" +
                    ", a.sOpenNowx" +
                    ", c.sBranchNm" +
                " FROM Branch_Opening a" + 
                    ", Branch_Others b" +
                        " LEFT JOIN Branch c" +
                            " ON b.sBranchCd = c.sBranchCd" +
                " WHERE a.sBranchCd = b.sBranchCd" +
                    " AND a.sBranchCd LIKE 'M%'" +
                    " AND a.dNotified IS NULL" +
                " ORDER BY a.sOpenNowx";
    }
    
    private String getSQ_AH(){
        return "SELECT" +
                    "  a.sAreaCode" +
                    ", a.sAreaDesc" +
                    ", a.sAreaMngr" +
                    ", c.sCompnyNm" +
                    ", b.sUserIDxx" +
                    ", b.sProdctID" +
                " FROM Branch_Area a" +
                    ", App_User_Master b" +
                        " LEFT JOIN Client_Master c" +
                            " ON b.sEmployNo = c.sClientID" +
                " WHERE a.sAreaMngr = b.sEmployNo" +
                    " AND IFNULL(a.sAreaMngr, '') <> ''" +
                    " AND b.sProdctID = 'gRider'";
    }
}