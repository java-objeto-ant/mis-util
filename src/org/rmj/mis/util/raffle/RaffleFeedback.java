package org.rmj.mis.util.raffle;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.WebClient;

/**
 * @author mac
 * @since 2022.10.04
 */
public class RaffleFeedback implements RaffleValidator{
    private GRiderX instance;
    
    private String sMessage;
    
    @Override
    public void setGRider(GRiderX foValue) {
        instance = foValue;
        
        if (instance == null){
            System.err.println("Application driver is not set.");
            System.exit(1);
        }
    }

    @Override
    public void setBranch(String fsValue) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean Run() {
        String lsSQL = "SELECT  " +
                            "  sTransNox" +
                            ", sMessagex" +
                            ", sMobileNo" +
                        " FROM SMS_Incoming" +
                        " WHERE LEFT(sMessagex, 5) LIKE 'TKSM3'" +
                            " AND (cTranStat IS NULL OR cTranStat = '0')";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            String [] lasValue;
            String lsUpdate;
            while (loRS.next()){
                lasValue = loRS.getString("sMessagex").split(" ");
                
                lsUpdate = "UPDATE SMS_Incoming SET cTranStat = '3'" +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                
                instance.beginTrans();
                if (lasValue[1].length() == 12){
                    lsSQL = "SELECT" +                     
                                "  a.sTransNox" +                
                                ", a.dTransact" +                
                                ", a.sBranchCd" +                
                                ", a.sSourceCd" +                
                                ", a.sSourceNo" +                
                                ", a.sReferNox" +                
                                ", a.sAcctNmbr" +                
                                ", a.sClientID" +                
                                ", a.sMobileNo" +                
                                ", a.cDivision" +                
                                ", a.sRandomNo" +                
                                ", a.sRaffleFr" +                
                                ", a.sRaffleTr" +                
                                ", a.nNoEntryx" +                
                                ", a.cMsgSentx" +   
                                ", a.cCltCnfrm" +
                                ", b.sFrstName" +
                            " FROM Raffle_With_SMS_Source a" +
                                " LEFT JOIN Client_Master b" +
                                    " ON a.sClientID = b.sClientID" +
                            " WHERE a.sTransNox = " + SQLUtil.toSQL(lasValue[1]);
                    
                    ResultSet loRS1 = instance.executeQuery(lsSQL);
                    
                    if (loRS1.next()){ //invalid transaction
                        if (loRS.getString("sMobileNo").replace("+63", "0").equals(loRS1.getString("sMobileNo").replace("+63", "0"))){                                                       
                            lsSQL = lasValue[0] + " " + lasValue[1] + 
                                    " raffle entry was successfully confirmed." +
                                    " Thank you for patronizing us.";
                            
                            boolean lbSent = sendMessage("GUANZON", "09176340516", lsSQL);

                            lsSQL = "INSERT INTO HotLine_Outgoing SET" +
                                    "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("HotLine_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode())) +
                                    ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) +
                                    ", sDivision = 'MIS'" +
                                    ", sMobileNo = " + SQLUtil.toSQL("09176340516") +
                                    ", sMessagex = " + SQLUtil.toSQL(lsSQL) +
                                    ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork("09176340516")) +
                                    ", dDueUntil = " + SQLUtil.toSQL(instance.getServerDate()) +
                                    ", cSendStat = '2'" +
                                    ", nNoRetryx = '1'" +
                                    ", sUDHeader = ''" +
                                    ", sReferNox = " + SQLUtil.toSQL(loRS1.getString("sSourceNo")) +
                                    ", sSourceCd = " + SQLUtil.toSQL(loRS1.getString("sSourceCd")) +
                                    ", cTranStat = " + SQLUtil.toSQL(lbSent ? "1" : "0") +
                                    ", nPriority = 1" +
                                    ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                            
                            if (instance.executeUpdate(lsSQL) <= 0){
                                setMessage(instance.getMessage() + "; " + instance.getErrMsg());
                                instance.rollbackTrans();
                                return false;
                            }    
                            
                            if (lbSent){
                                lsSQL = "UPDATE Raffle_With_SMS_Source SET" +
                                            "  cCltCnfrm = '1'" +
                                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS1.getString("sTransNox"));

                                if (instance.executeUpdate(lsSQL) <= 0){
                                    setMessage(instance.getMessage() + "; " + instance.getErrMsg());
                                    instance.rollbackTrans();
                                    return false;
                                }                            

                                lsUpdate = "UPDATE SMS_Incoming SET cTranStat = '1'" +
                                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                            } else
                                lsUpdate = "";
                        }
                    }
                }
                
                if (!lsUpdate.isEmpty()){
                    if (instance.executeUpdate(lsUpdate) <= 0){
                        setMessage(instance.getMessage() + "; " + instance.getErrMsg());
                        instance.rollbackTrans();
                        return false;
                    }
                }
                
                instance.commitTrans();
            }
        } catch (SQLException e) {
            setMessage(e.getMessage());
            return false;
        }
        
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
    
    private boolean sendMessage(String fsMaskName, String fsMobileNo, String fsMessage){        
        String fsURL = "https://restgk.guanzongroup.com.ph/system/masking/sendSMS.php";
        
        String clientid = instance.getClientID(); //this must be replaced based on the client id using it
        String productid = instance.getProductID(); //this must be replaced based on the product id using it
        String imei = MiscUtil.getPCName(); //this must be replaced based on the computer name using it
        String userid = instance.getUserID(); //this must be replaced based on the user id using it
        
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
                System.err.println(String.valueOf(loJSON.get("code")) + " - " + (String) loJSON.get("message"));
                return false;
            }
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
