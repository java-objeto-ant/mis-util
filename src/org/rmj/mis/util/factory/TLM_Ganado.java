package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
/**
 *
 * @author Michael Cuison
 * @since  2023.09.07
 */
public class TLM_Ganado implements UtilityValidator{  
    private final String MASTER_TABLE = "Ganado_Online";
    
    private GRiderX _instance;
    private String _message;
    
    @Override
    public void setGRider(GRiderX foValue) {
        _instance = foValue;
    }

    @Override
    public boolean Run() {
        if (_instance == null){
            _message = "Application driver is not set.";
            return false;
        }
        
        String lsClientID;
        String lsMobileNo;
        
        String lsSQL = getSQ_Ganado();
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        try {
            JSONObject loJSON;
            JSONParser loParser = new JSONParser();
            
            while (loRS.next()){
                _instance.beginTrans();
                
                loJSON = (JSONObject) loParser.parse(loRS.getString("sCltInfox"));
                
                lsClientID = loRS.getString("sClientID");
                lsMobileNo = ((String) loJSON.get("sMobileNo")).trim();
                
                //process client information
                if (lsClientID.isEmpty()){    
                    lsSQL = ((String) loJSON.get("sLastName")).trim() + ", " + ((String) loJSON.get("sFrstName")).trim();
                    if (!((String) loJSON.get("sSuffixNm")).trim().isEmpty()){
                        lsSQL += " " + ((String) loJSON.get("sSuffixNm")).trim();
                    }
                    if (!((String) loJSON.get("sMiddName")).trim().isEmpty()){
                        lsSQL += " " + ((String) loJSON.get("sMiddName")).trim();
                    }

                    lsClientID = MiscUtil.getNextCode("Client_Master", "sClientID", true, _instance.getConnection(), "MX01");

                    lsSQL = "INSERT INTO Client_Master SET " + 
                                "  sClientID = " + SQLUtil.toSQL(lsClientID) +
                                ", sLastName = " + SQLUtil.toSQL(((String) loJSON.get("sLastName")).trim()) +
                                ", sFrstName = " + SQLUtil.toSQL(((String) loJSON.get("sFrstName")).trim()) +
                                ", sMiddName = " + SQLUtil.toSQL(((String) loJSON.get("sMiddName")).trim()) +
                                ", sMaidenNm = " + SQLUtil.toSQL(((String) loJSON.get("sMaidenNm")).trim()) +
                                ", sSuffixNm = " + SQLUtil.toSQL(((String) loJSON.get("sSuffixNm")).trim()) +
                                ", cGenderCd = '0'" +
                                ", cCvilStat = '0'" +
                                ", sCitizenx = '01'" +
                                ", dBirthDte = " + SQLUtil.toSQL(((String) loJSON.get("dBirthDte")).trim()) +
                                ", sBirthPlc = " + SQLUtil.toSQL(((String) loJSON.get("sBirthPlc")).trim()) +
                                ", sHouseNox = " + SQLUtil.toSQL(((String) loJSON.get("sHouseNox")).trim()) +
                                ", sAddressx = " + SQLUtil.toSQL(((String) loJSON.get("sAddressx")).trim()) +
                                ", sTownIDxx = " + SQLUtil.toSQL(((String) loJSON.get("sTownIDxx")).trim()) +
                                ", sBrgyIDxx = ''" +
                                ", sPhoneNox = ''" +
                                ", sMobileNo = " + SQLUtil.toSQL(lsMobileNo) +
                                ", sEmailAdd = " + SQLUtil.toSQL(((String) loJSON.get("sEmailAdd")).trim()) +
                                ", cEducLevl = '6'" +
                                ", sRelgnIDx = ''" +
                                ", sTaxIDNox = ''" +
                                ", sSSSNoxxx = ''" +
                                ", sAddlInfo = ''" +
                                ", sCompnyNm = " + SQLUtil.toSQL(lsSQL) +
                                ", sOccptnID = ''" +
                                ", sOccptnOT = ''" +
                                ", nGrssIncm = 0" +
                                ", sClientNo = ''" +
                                ", sSpouseID = ''" +
                                ", sFatherID = ''" +
                                ", sMotherID = ''" +
                                ", sSiblngID = ''" +
                                ", cClientTp = '0'" +
                                ", cLRClient = '0'" +
                                ", cMCClient = '0'" +
                                ", cSCClient = '0'" +
                                ", cSPClient = '0'" +
                                ", cCPClient = '0'" +
                                ", cRecdStat = '1'" +
                                ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                                ", dModified = " + SQLUtil.toSQL(_instance.getServerDate()); 
                    
                    if (_instance.executeQuery(lsSQL, "Client_Master", _instance.getBranchCode(), "") <= 0){
                        _instance.rollbackTrans();
                        setMessage("Unable to save Client Information. " + loRS.getString("sTransNox"));
                        return false;
                    }
                    
                    lsSQL = "INSERT INTO Client_Mobile SET" +
                            "  sClientID = " + SQLUtil.toSQL(lsClientID) +
                            ", nEntryNox = '1'" +
                            ", sMobileNo = " + SQLUtil.toSQL(lsMobileNo) +
                            ", nPriority = 1" +
                            ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork(lsMobileNo)) +
                            ", cRecdStat = '1'";
                    
                    if (_instance.executeQuery(lsSQL, "Client_Mobile", _instance.getBranchCode(), "") <= 0){
                        _instance.rollbackTrans();
                        setMessage("Unable to save Client Information. " + loRS.getString("sTransNox"));
                        return false;
                    }

                    lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                                "  sClientID = " + SQLUtil.toSQL(lsClientID) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")); 
                   
                    if (_instance.executeQuery(lsSQL, MASTER_TABLE, _instance.getBranchCode(), "") <= 0){
                        _instance.rollbackTrans();
                        setMessage("Unable to save Ganado Information. " + loRS.getString("sTransNox"));
                        return false;
                    }
                    //end - process client information
                }
                
                //add ganado to tlm leads
                lsSQL = "INSERT INTO Call_Outgoing SET" + 
                        "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, _instance.getConnection(), _instance.getBranchCode())) +
                        ", dTransact = " + SQLUtil.toSQL(_instance.getServerDate()) +
                        ", sClientID = " + SQLUtil.toSQL(lsClientID) +
                        ", sMobileNo = " + SQLUtil.toSQL(lsMobileNo) +
                        ", sRemarksx = ''" + 
                        ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")) +
                        ", sSourceCD = " + SQLUtil.toSQL("GNDO") +
                        ", cTranStat = '0'" + 
                        ", sAgentIDx = ''" + 
                        ", nNoRetryx = 0" +
                        ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork(lsMobileNo)) +
                        ", cCallStat = '0'" +
                        ", cTLMStatx = '0'" +
                        ", cSMSStatx = '0'"  +
                        ", nSMSSentx = 0" + 
                        ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                        ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());
                
                if (_instance.executeQuery(lsSQL, MASTER_TABLE, _instance.getBranchCode(), "") <= 0){
                    _instance.rollbackTrans();
                    setMessage("Unable to save Call_Outgoing. " + loRS.getString("sTransNox"));
                    return false;
                }
                
                lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cTranStat = '1'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")); 

                if (_instance.executeQuery(lsSQL, MASTER_TABLE, _instance.getBranchCode(), "") <= 0){
                    _instance.rollbackTrans();
                    setMessage("Unable to save Ganado Information. " + loRS.getString("sTransNox"));
                    return false;
                }
                
                _instance.commitTrans();
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return false;
        }
        
        return true;
    }

    @Override
    public String getMessage() {
        return _message;
    }

    @Override
    public void setMessage(String fsValue) {
        _message = fsValue;
    }
    
    private String getSQ_Ganado(){
        return "SELECT" +
                    "  sTransNox" +
                    ", dTransact" +
                    ", sClientNm" +
                    ", cGanadoTp" +
                    ", cPaymForm" +
                    ", sCltInfox" +
                    ", sPrdctInf" +
                    ", sPaymInfo" +
                    ", dTargetxx" +
                    ", dFollowUp" +
                    ", sRemarksx" +
                    ", sReferdBy" +
                    ", sRelatnID" +
                    ", dCreatedx" +
                    ", nLatitude" +
                    ", nLongitud" +
                    ", IFNULL(sClientID, '') sClientID" +
                    ", cTranStat" +
                " FROM " + MASTER_TABLE +
                " WHERE cTranStat = '0'" +
                    " AND cGanadoTp = '1'" + //mc only
                    " AND (dFollowUp IS NULL OR dFollowUp <= CURRENT_TIMESTAMP())";
    }
}
