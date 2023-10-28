package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;

public class TLM_CA implements UtilityValidator{
    private final String SOURCECD = "MCCA";
    private final String START_DATE = "2021-05-01"; //"2019-09-01"
    private final int DELAY = -3;

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
    public boolean Run() {
        System.out.println("Updating leads that already purchased....");
        if (!updateReleased()) return false;
        
        System.out.println("Converting approved by more than " + Math.abs(DELAY) +" days applications to leads....");
        return create(true);
    }

    @Override
    public void setMessage(String fsValue) {
        sMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return sMessage;
    }
    
    private boolean updateReleased(){       
        String lsSQL = "SELECT *" +
                        " FROM Call_Outgoing" +
                        " WHERE sSourceCd = 'MCCA'" +
                            " AND cTranStat IN('0', '1')" +
                        " ORDER BY sTransNox DESC";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        ResultSet loRSCI;
        int lnRow = 0;
        try {
            while(loRS.next()){
                lsSQL = "SELECT * FROM MC_Credit_Application" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sReferNox"));
                
                loRSCI = instance.executeQuery(lsSQL);
                
                if (loRSCI.next()){
                    //already selected.
                    
                    if (loRSCI.getString("cTranStat").equals("4")){
                        lsSQL = "UPDATE Call_Outgoing SET" +
                                    "  cTranStat = '4'" +
                                    ", sRemarksx = 'Already selected before call.'" +
                                    ", sModified = " + SQLUtil.toSQL(instance.getUserID()) + 
                                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate()) + 
                                " WHERE sReferNox = " + SQLUtil.toSQL(loRSCI.getString("sTransNox")) +
                                    " AND sSourceCd = 'MCCA'";
                        
                        instance.beginTrans();
                        if (instance.executeQuery(lsSQL, "Call_Outgoing", instance.getBranchCode(), "") <= 0){
                            sMessage = instance.getErrMsg() + "\n" + instance.getMessage();
                            instance.rollbackTrans();

                            System.err.println(sMessage);
                            return false;
                        }
                        instance.commitTrans();
                        
                        lnRow = lnRow + 1;
                    }
                }
            }
        } catch (SQLException ex) {
            sMessage = ex.getMessage();
            System.err.println(sMessage);
            return false;
        }
        
        sMessage  = lnRow + " record(s) have been updated to selected.";
        System.out.println(sMessage);
        return true;
    }
    
    public boolean create(boolean fbForceExe){
        String lsSQL;
        int lnRow = 0;
        
        if (!fbForceExe){
            System.out.println("Normal mode create...");
            lsSQL = "SELECT *" +
                    " FROM Call_Outgoing" +
                    " WHERE dTransact LIKE " + 
                        SQLUtil.toSQL(SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE) + "%");
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) > 0){
                sMessage = "Schedule for the day has been created.";
                return true;
            }
        } else 
             System.out.println("Forced mode create...");
        
        lsSQL = "SELECT" +
                    "  sBranchCD" +
                    ", sBranchNm" +
                " FROM Branch" +
                " WHERE sBranchCd LIKE 'M%'" +
                    " AND cRecdStat = '1'" + 
                " ORDER BY sBranchCd";
        
        ResultSet loRSBranch = instance.executeQuery(lsSQL);
        
        try {
            ResultSet loRSCI;
            
            //load mc branches
            while(loRSBranch.next()){
                System.out.println(loRSBranch.getString("sBranchCD") + " - " + loRSBranch.getString("sBranchNm"));
                
                lsSQL = SQLUtil.dateFormat(instance.getServerDate(), "yy");
                
                lsSQL = MiscUtil.addCondition(getSQ_Master(), 
                            "a.sTransNox LIKE " + SQLUtil.toSQL(loRSBranch.getString("sBranchCd") + lsSQL + "%"));
                
                loRSCI = instance.executeQuery(lsSQL);
                
                instance.beginTrans();
                while (loRSCI.next()){
                    lsSQL = getSQ_Insert(loRSCI.getString("sClientID"), 
                                            loRSCI.getString("sMobileNo"), 
                                            loRSCI.getString("sTransNox"));
                    
                    if (!lsSQL.equals("")){
                        //insert into call outgoing
                        if (instance.executeQuery(lsSQL, "Call_Outgoing", instance.getBranchCode(), "") <= 0){
                            sMessage = instance.getErrMsg() + "\n" + instance.getMessage();
                            instance.rollbackTrans();

                            System.err.println(sMessage);
                            return false;
                        }
                        
                        //update credit application table
                        lsSQL = "UPDATE MC_Credit_Application SET" + 
                                    "  cTLMStatx = '1'" + 
                                    ", sModified = " + SQLUtil.toSQL(instance.getUserID()) + 
                                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate()) + 
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRSCI.getString("sTransNox"));
                        
                        if (instance.executeQuery(lsSQL, "MC_Credit_Application", instance.getBranchCode(), "") <= 0){
                            sMessage = instance.getErrMsg() + "\n" + instance.getMessage();
                            instance.rollbackTrans();

                            System.err.println(sMessage);
                            return false;
                        }
                        
                        lnRow = lnRow + 1;
                    }
                }                
                instance.commitTrans();
            }
        } catch (SQLException ex) {
            instance.rollbackTrans();
            sMessage = ex.getMessage();
            System.err.println(sMessage);
            return false;
        }
        
        if (lnRow > 0)
            sMessage = "Leads created successfully -->> " + lnRow + " record(s)";
        else
            sMessage = "No approved credit app for call leads yet...";
            
        System.out.println(sMessage);
        return true;
    }
    
    private String getSQ_Insert(String fsClientID,
                                String fsMobileNo,
                                String fsTransNox){
        
        String lsNetwork = CommonUtils.classifyNetwork(fsMobileNo);
        
        if (lsNetwork.equals("")) return "";
        
        String lsAgentIDx = getAgentIDx(SOURCECD, fsTransNox);
        
        if (lsAgentIDx.isEmpty())
            return "INSERT INTO Call_Outgoing SET" +
                    "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode())) +
                    ", dTransact = " + SQLUtil.toSQL(instance.getServerDate())+
                    ", sClientID = " + SQLUtil.toSQL(fsClientID) +
                    ", sMobileNo = " + SQLUtil.toSQL(fsMobileNo) +
                    ", sRemarksx = ''" +
                    ", sReferNox = " + SQLUtil.toSQL(fsTransNox) +
                    ", sSourceCd = " + SQLUtil.toSQL(SOURCECD)+
                    ", sApprovCd = ''" +
                    ", cTranStat = '0'" +
                    ", sAgentIDx = ''" +
                    ", dCallStrt = NULL" +
                    ", dCallEndx = NULL" +
                    ", nNoRetryx = 0" +
                    ", cSubscrbr = " + SQLUtil.toSQL(lsNetwork) +
                    ", cTLMStatx = ''" +
                    ", cSMSStatx = '0'" +
                    ", nSMSSentx = 0" +
                    ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
        else
            return "INSERT INTO Call_Outgoing SET" +
                    "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode())) +
                    ", dTransact = " + SQLUtil.toSQL(instance.getServerDate())+
                    ", sClientID = " + SQLUtil.toSQL(fsClientID) +
                    ", sMobileNo = " + SQLUtil.toSQL(fsMobileNo) +
                    ", sRemarksx = ''" +
                    ", sReferNox = " + SQLUtil.toSQL(fsTransNox) +
                    ", sSourceCd = " + SQLUtil.toSQL(SOURCECD)+
                    ", sApprovCd = ''" +
                    ", cTranStat = '1'" +
                    ", sAgentIDx = " + SQLUtil.toSQL(lsAgentIDx) +
                    ", dCallStrt = NULL" +
                    ", dCallEndx = NULL" +
                    ", nNoRetryx = 0" +
                    ", cSubscrbr = " + SQLUtil.toSQL(lsNetwork) +
                    ", cTLMStatx = ''" +
                    ", cSMSStatx = '0'" +
                    ", nSMSSentx = 0" +
                    ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
    }
    
    private String getSQ_Master(){
//        return "SELECT" +
//                    "  a.sTransNox" +
//                    ", a.dAppliedx" +
//                    ", a.sClientID" + 
//                    ", b.sCompnyNm" +
//                    ", b.sMobileNo" +
//                    ", a.sQMatchNo" +
//                    ", c.sTransNox" +
//                " FROM MC_Credit_Application a" +
//                    " LEFT JOIN Client_Master b" +
//                        " ON a.sClientID = b.sClientID" +
//                    " LEFT JOIN MC_SO_Master c" +
//                        " ON c.sTransNox LIKE CONCAT(LEFT(a.sTransNox, 4), '%')" +
//                            " AND c.dTransact >= a.dAppliedx" +
//                            " AND c.cPaymForm = '2'" +
//                            " AND c.cTranStat NOT IN ('3', '7')" +
//                            " AND a.sTransNox = c.sApplicNo" +
//                " WHERE  a.cTranStat = '2'" +
//                    " AND a.cTLMStatx = '0'" +
//                    " AND LEFT(a.sQMatchNo, 2) = 'CI'" +
//                    " AND a.dAppliedx >= " + SQLUtil.toSQL(START_DATE) +
//                    " AND a.dAppliedx <= " + SQLUtil.toSQL(MiscUtil.dateAdd(instance.getServerDate(), DELAY)) + 
//                    " AND a.sTransNox NOT IN (" +
//                        "SELECT sReferNox FROM Call_Outgoing" +
//                        " WHERE sSourceCd = " + SQLUtil.toSQL(SOURCECD) + ")" + 
//                " HAVING ISNULL(c.sTransNox)" +
//                " ORDER BY a.dAppliedx";
        return "SELECT" +
                    "  a.sTransNox" +
                    ", a.dAppliedx" +
                    ", a.sClientID" + 
                    ", b.sCompnyNm" +
                    ", b.sMobileNo" +
                    ", a.sQMatchNo" +
                " FROM MC_Credit_Application a" +
                    " LEFT JOIN Client_Master b" +
                        " ON a.sClientID = b.sClientID" +
                " WHERE  a.cTranStat = '2'" +
                    " AND a.cTLMStatx = '0'" +
                    " AND LEFT(a.sQMatchNo, 2) = 'CI'" +
                    " AND a.dAppliedx >= DATE_SUB(NOW(), INTERVAL 1 MONTH)" +
                    " AND a.dAppliedx <= " + SQLUtil.toSQL(MiscUtil.dateAdd(instance.getServerDate(), DELAY)) + 
                    " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01'" + 
                            " OR dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY)" + 
                                " AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))" +
                " ORDER BY a.dAppliedx";
    }
    
    //get the last agent called the transaction
    private String getAgentIDx(String fsSourceCD, String fsReferNox){
        String lsSQL = "SELECT sAgentIDx FROM Call_Outgoing" +
                        " WHERE sSourceCd = " + SQLUtil.toSQL(fsSourceCD) +
                            " AND sReferNox = " + SQLUtil.toSQL(fsReferNox) +
                            " AND cTranStat = '2'" +
                        " ORDER BY sTransNox DESC LIMIT 1";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            if (loRS.next())
                return loRS.getString("sAgentIDx");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
