package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.StringHelper;
import org.rmj.appdriver.agentfx.WebClient;
import org.rmj.lib.net.LogWrapper;
import org.rmj.mis.util.sms.MaskSMS;

public class CreatePasscode {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("PassCode", "GSecure.log");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("gRider");
        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        try {
            String lsSQL;
            ResultSet loRS;
            
            lsSQL = "SELECT *" +
                    " FROM HotLine_Outgoing" +
                    " WHERE sSourceCd = 'PASS'" +
                        " AND dTransact = " + SQLUtil.toSQL(SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)) +
                    "  LIMIT 1";

            loRS = instance.executeQuery(lsSQL);

            if (MiscUtil.RecordCount(loRS) > 0) System.exit(0);

            String lsCode = StringHelper.prepad(String.valueOf(MiscUtil.getRandom(1, 9999)), 4, '0') ;
            System.out.println(lsCode);

            lsSQL = "SELECT"+
                        "  IFNULL(a.sEmployID, '') sEmployID"+
                        ", a.sMobileNo"+
                        ", IFNULL(CONCAT(LEFT(b.sFrstName, 1), LEFT(b.sMiddName, 1), LEFT(b.sLastName, 1)), 'GUA') xInitialx"+
                    " FROM Employee_Text_Alert a"+
                        " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID"+
                    " WHERE a.cStopAlrt = '0'"+
                        " AND IF(a.dDateFrom = '0000-00-00', '1=1', (a.dDateFrom >= " + SQLUtil.toSQL(instance.getServerDate()) + " OR a.dDateThru <= " + SQLUtil.toSQL(instance.getServerDate()) + "))";
            System.out.println(lsSQL);

            loRS = instance.executeQuery(lsSQL);

            String lsTransNox;
            int lnCtr = 1;
            
            ArrayList recipients;
            MaskSMS processor;
            
            instance.beginTrans();
            
            while (loRS.next()){
                lsTransNox = MiscUtil.getNextCode("HotLine_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode());
                
                lsSQL = loRS.getString("xInitialx") + "-" + String.valueOf(lnCtr) + SQLUtil.dateFormat(instance.getServerDate(), "dd") +
                        " Guanzon Group\n" + "G*-" + lsCode + " is your " + SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_MEDIUM_DATE) +
                        " PASS CODE. Thank you and be safe. Ref#: " + lsTransNox;
                
                if (!loRS.getString("sEmployID").isEmpty()){
                    sendToGCircle(instance, loRS.getString("sEmployID"), lsSQL);
                }
                
                if ("09778235734»09178236807»09171450191»09175693337»09171640023»09176340516»09327259595»09064076295»09064076294»09053120008»09178911333»09178517555»09178806899»09171703182»09327259595»09175695800»09177101514»09778237299".contains(loRS.getString("sMobileNo"))){
                    recipients = new ArrayList();
                    recipients.add(loRS.getString("sMobileNo"));
                    
                    processor = new MaskSMS(instance, logwrapr);
                    processor.setMaskName("GUANZON");
                    processor.setSMS(lsSQL);
                    processor.setRecipient(recipients);
                    processor.saveMessage(true);

                    if (!processor.sendMessage()){
                        logwrapr.severe(processor.getMessage());
                    }
                } else {
                    lsSQL = "INSERT INTO HotLine_Outgoing SET" +
                            "  sTransNox = " + SQLUtil.toSQL(lsTransNox) +
                            ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) +
                            ", sDivision = " + SQLUtil.toSQL("MIS") +
                            ", sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                            ", sMessagex = " + SQLUtil.toSQL(lsSQL) +
                            ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork(loRS.getString("sMobileNo"))) +
                            ", dDueUntil = " + SQLUtil.toSQL(instance.getServerDate()) +
                            ", cSendStat = '0'" +
                            ", nNoRetryx = 0" +
                            ", nPriority = 1" +
                            ", sUDHeader = ''" +
                            ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                            ", sSourceCd = 'PASS'" +
                            ", cTranStat = '0'" +
                            ", sModified = ''" + 
                            ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                
                    if (instance.executeQuery(lsSQL, "HotLine_Outgoing", instance.getBranchCode(), "") <= 0){
                        instance.rollbackTrans();
                        logwrapr.severe(instance.getErrMsg() + ";" + instance.getMessage());
                        System.exit(1);
                    }
                }

                lnCtr++;
            }
            
            instance.commitTrans();
        } catch (SQLException e) {
            instance.rollbackTrans();
            logwrapr.severe(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
    }
    
    public static void sendToGCircle(GRider foGRider, String fsEmployID, String fsMessage) throws SQLException{
        String lsSQL = "SELECT a.sProdctID, a.sUserIDxx" +
                        " FROM App_User_Master a" +
                            ", Employee_Master001 b" +
                        " WHERE a.sEmployNo = b.sEmployID" +
                                " AND a.sProdctID = 'gRider'" +
                                " AND a.sEmployNo = " + SQLUtil.toSQL(fsEmployID);
        
        ResultSet loRS = foGRider.executeQuery(lsSQL);
        JSONArray rcpts = new JSONArray();
        JSONObject rcpt;

        if (MiscUtil.RecordCount(loRS) <= 0) return;
        
        while (loRS.next()){
            rcpt = new JSONObject();
            rcpt.put("app", loRS.getString("sProdctID"));
            rcpt.put("user",loRS.getString("sUserIDxx"));
            rcpts.add(rcpt);
        }
        
        SendRegularSystemNotification(rcpts, 
                                        SQLUtil.dateFormat(foGRider.getServerDate(), SQLUtil.FORMAT_MEDIUM_DATE) + " PASS CODE", 
                                fsMessage);
    }
    
    public static boolean SendRegularSystemNotification(JSONArray rcpts,
                                                        String title,
                                                        String message){
        try{
            String sURL = "https://restgk.guanzongroup.com.ph/notification/send_request_system.php";
            Calendar calendar = Calendar.getInstance();
            //Create the header section needed by the API
            Map<String, String> headers =
                    new HashMap<String, String>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("g-api-id", "gRider");
            headers.put("g-api-imei", "356060072281722");
            headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));
            headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
            headers.put("g-api-user", "GAP0190001");
            headers.put("g-api-mobile", "09171870011");
            headers.put("g-api-token", "cPYKpB-pPYM:APA91bE82C4lKZduL9B2WA1Ygd0znWEUl9rM7pflSlpYLQJq4Nl9l5W4tWinyy5RCLNTSs3bX3JjOVhYnmCpe7zM98cENXt5tIHwW_2P8Q3BXI7gYtEMTJN5JxirOjNTzxWHkWDEafza");

            JSONObject param = new JSONObject();
            param.put("type", "00000");
            param.put("parent", null);
            param.put("title", title);
            param.put("message", message);
            param.put("rcpt", rcpts);
            param.put("infox", null);

            String response = WebClient.sendHTTP(sURL, param.toJSONString(), (HashMap<String, String>) headers);
            if(response == null){
                System.out.println("HTTP Error detected: " + System.getProperty("store.error.info"));
                System.exit(1);
            }

            System.out.println(response);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
