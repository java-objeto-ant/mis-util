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
public class RaffleNotify implements RaffleValidator{
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
        String lsSQL = "SELECT" +                     
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
                            ", b.sFrstName" +
                        " FROM Raffle_With_SMS_Source a" +
                            " LEFT JOIN Client_Master b" +
                                " ON a.sClientID = b.sClientID" +
                        " WHERE cMsgSentx = '0'" +
                            " AND sSourceCd IN ('SPSl')";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        int lnNoEntryx;
        String lsTicketsx;
        
        try {
            while (loRS.next()){
                lnNoEntryx = loRS.getInt("nNoEntryx");
                
                if (lnNoEntryx == 1){
                    lsTicketsx = "ticket, " + loRS.getString("cDivision") + loRS.getString("sRandomNo") + loRS.getString("sRaffleFr");
                } else {
                    lsTicketsx = "tickets, " + loRS.getString("cDivision") + loRS.getString("sRandomNo") + loRS.getString("sRaffleFr") +
                                    " to " + loRS.getString("cDivision") + loRS.getString("sRandomNo") + loRS.getString("sRaffleTr");
                }   
                
                lsSQL = "TKSM3 " + loRS.getString("sTransNox") +  
                        "\nHi " + loRS.getString("sFrstName") + "! ";
                
                switch (loRS.getString("sSourceCd")){
                    case "LRxx":
                    case "LRPR":
                    case "ORxx":
                    case "PRxx":
                        lsSQL += "Your MONTHLY PAYMENT last " + loRS.getString("dTransact") +
                                    " w/ refer no. " + loRS.getString("sReferNox") +
                                    " earns " +  lnNoEntryx + " raffle " + lsTicketsx + ".";
                        break;
                    case "MCSl":
                        lsSQL += "Your MC PURCHASE last " + loRS.getString("dTransact") +
                                    " w/ refer no. " + loRS.getString("sReferNox") +
                                    " earns " +  lnNoEntryx + " raffle " + lsTicketsx + ".";
                        break;
                    case "SPSl":
                        lsSQL += "Your SP PURCHASE last " + loRS.getString("dTransact") +
                                    " w/ refer no. " + loRS.getString("sReferNox") +
                                    " earns " +  lnNoEntryx + " raffle " + lsTicketsx + ".";
                        break;
                    case "MPSl":
                        lsSQL += "Your MP PURCHASE last " + loRS.getString("dTransact") +
                                    " w/ refer no. " + loRS.getString("sReferNox") +
                                    " earns " +  lnNoEntryx + " raffle " + lsTicketsx + ".";
                        break;
                    default:
                        lsSQL = "";
                }
                
                if (!lsSQL.isEmpty()){
                    lsSQL += "\n\nTo confirm your raffle participation please forward this message to 09479906531.";
                    lsTicketsx = lsSQL;
                    
                    //loRS.getString("sMobileNo")
                    boolean lbSent = sendMessage("GUANZON", "09176340516", lsSQL);
                    
                    lsSQL = MiscUtil.getNextCode("HotLine_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode());

                    lsSQL = "INSERT INTO HotLine_Outgoing SET" +
                            "  sTransNox = " + SQLUtil.toSQL(lsSQL) +
                            ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) +
                            ", sDivision = 'MIS'" +
                            ", sMobileNo = " + SQLUtil.toSQL("09176340516") +
                            ", sMessagex = " + SQLUtil.toSQL(lsTicketsx) +
                            ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork("09176340516")) +
                            ", dDueUntil = " + SQLUtil.toSQL(instance.getServerDate()) +
                            ", cSendStat = '2'" +
                            ", nNoRetryx = '1'" +
                            ", sUDHeader = ''" +
                            ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sSourceNo")) +
                            ", sSourceCd = " + SQLUtil.toSQL(loRS.getString("sSourceCd")) +
                            ", cTranStat = " + SQLUtil.toSQL(lbSent ? "1" : "0") +
                            ", nPriority = 1" +
                            ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                    
                    instance.beginTrans();
                    if (instance.executeUpdate(lsSQL) <= 0){
                        setMessage(instance.getMessage() + "; " + instance.getErrMsg());
                        instance.rollbackTrans();
                        return false;
                    }
                    
                    if (lbSent){
                        lsSQL = "UPDATE Raffle_With_SMS_Source SET" +
                                    "  cMsgSentx = '1'" +
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                        
                        if (instance.executeUpdate(lsSQL) <= 0){
                            setMessage(instance.getMessage() + "; " + instance.getErrMsg());
                            instance.rollbackTrans();
                            return false;
                        }
                    }

                    instance.commitTrans();
                }
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
