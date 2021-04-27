package org.rmj.mis.util.tlm;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;

public class CI2TLM {
    private final String SOURCECD = "MCCA";
    private final String START_DATE = "2019-09-01";
    private final int DELAY = -3;
    
    private GRider poGRider;
    private String psMessage;

    public CI2TLM(GRider foGRider){
        poGRider = foGRider;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public boolean UpdateReleased(){
        if (poGRider == null){
            psMessage = "Application driver is not set...";
            return false;
        }
        
        String lsSQL = "SELECT *" +
                        " FROM Call_Outgoing" +
                        " WHERE sSourceCd = 'MCCA'" +
                            " AND cTranStat IN('0', '1')" +
                        " ORDER BY sTransNox DESC";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        ResultSet loRSCI;
        int lnRow = 0;
        try {
            while(loRS.next()){
                lsSQL = "SELECT * FROM MC_Credit_Application" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sReferNox"));
                
                loRSCI = poGRider.executeQuery(lsSQL);
                
                if (loRSCI.next()){
                    //already selected.
                    
                    if (loRSCI.getString("cTranStat").equals("4")){
                        lsSQL = "UPDATE Call_Outgoing SET" +
                                    "  cTranStat = '4'" +
                                    ", sRemarksx = 'Already selected before call.'" +
                                    ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) + 
                                    ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
                                " WHERE sReferNox = " + SQLUtil.toSQL(loRSCI.getString("sTransNox")) +
                                    " AND sSourceCd = 'MCCA'";
                        
                        poGRider.beginTrans();
                        if (poGRider.executeQuery(lsSQL, "Call_Outgoing", poGRider.getBranchCode(), "") <= 0){
                            psMessage = poGRider.getErrMsg() + "\n" + poGRider.getMessage();
                            poGRider.rollbackTrans();

                            System.err.println(psMessage);
                            return false;
                        }
                        poGRider.commitTrans();
                        
                        lnRow = lnRow + 1;
                    }
                }
            }
        } catch (SQLException ex) {
            psMessage = ex.getMessage();
            System.err.println(psMessage);
            return false;
        }
        
        psMessage  = lnRow + " record(s) have been updated to selected.";
        System.out.println(psMessage);
        return true;
    }

    public boolean Create(boolean fbForceExe){
        if (poGRider == null){
            psMessage = "Application driver is not set...";
            return false;
        }
        
        String lsSQL;
        int lnRow = 0;
        
        if (!fbForceExe){
            System.out.println("Normal mode create...");
            lsSQL = "SELECT *" +
                    " FROM Call_Outgoing" +
                    " WHERE dTransact LIKE " + 
                        SQLUtil.toSQL(SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_SHORT_DATE) + "%");
            
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) > 0){
                psMessage = "Schedule for the day has been created.";
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
        
        ResultSet loRSBranch = poGRider.executeQuery(lsSQL);
        
        try {
            ResultSet loRSCI;
            
            //load mc branches
            while(loRSBranch.next()){
                System.out.println(loRSBranch.getString("sBranchNm"));
                
                lsSQL = SQLUtil.dateFormat(poGRider.getServerDate(), "yy");
                
                lsSQL = MiscUtil.addCondition(getSQ_Master(), 
                            "a.sTransNox LIKE " + SQLUtil.toSQL(loRSBranch.getString("sBranchCd") + lsSQL + "%"));
                
                loRSCI = poGRider.executeQuery(lsSQL);
                
                poGRider.beginTrans();
                while (loRSCI.next()){
                    lsSQL = getSQ_Insert(loRSCI.getString("sClientID"), 
                                            loRSCI.getString("sMobileNo"), 
                                            loRSCI.getString("sTransNox"));
                    
                    if (!lsSQL.equals("")){
                        //insert into call outgoing
                        if (poGRider.executeQuery(lsSQL, "Call_Outgoing", poGRider.getBranchCode(), "") <= 0){
                            psMessage = poGRider.getErrMsg() + "\n" + poGRider.getMessage();
                            poGRider.rollbackTrans();

                            System.err.println(psMessage);
                            return false;
                        }
                        
                        //update credit application table
                        lsSQL = "UPDATE MC_Credit_Application SET" + 
                                    "  cTLMStatx = '1'" + 
                                    ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) + 
                                    ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRSCI.getString("sTransNox"));
                        
                        if (poGRider.executeQuery(lsSQL, "MC_Credit_Application", poGRider.getBranchCode(), "") <= 0){
                            psMessage = poGRider.getErrMsg() + "\n" + poGRider.getMessage();
                            poGRider.rollbackTrans();

                            System.err.println(psMessage);
                            return false;
                        }
                        
                        lnRow = lnRow + 1;
                    }
                }                
                poGRider.commitTrans();
            }
        } catch (SQLException ex) {
            poGRider.rollbackTrans();
            psMessage = ex.getMessage();
            System.err.println(psMessage);
            return false;
        }
        
        if (lnRow > 0)
            psMessage = "Leads created successfully -->> " + lnRow + " record(s)";
        else
            psMessage = "No approved credit app for call leads yet...";
            
        System.out.println(psMessage);
        return true;
    }
    
    private String getSQ_Insert(String fsClientID,
                                String fsMobileNo,
                                String fsTransNox){
        
        String lsNetwork = CommonUtils.classifyNetwork(fsMobileNo);
        
        if (lsNetwork.equals("")) return "";
        
        return "INSERT INTO Call_Outgoing SET" +
                "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode())) +
                ", dTransact = " + SQLUtil.toSQL(poGRider.getServerDate())+
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
                ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) +
                ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", a.dAppliedx" +
                    ", a.sClientID" + 
                    ", b.sCompnyNm" +
                    ", b.sMobileNo" +
                    ", a.sQMatchNo" +
                    ", c.sTransNox" +
                " FROM MC_Credit_Application a" +
                    " LEFT JOIN Client_Master b" +
                        " ON a.sClientID = b.sClientID" +
                    " LEFT JOIN MC_SO_Master c" +
                        " ON c.sTransNox LIKE CONCAT(LEFT(a.sTransNox, 4), '%')" +
                            " AND c.dTransact >= a.dAppliedx" +
                            " AND c.cPaymForm = '2'" +
                            " AND c.cTranStat NOT IN ('3', '7')" +
                            " AND a.sTransNox = c.sApplicNo" +
                " WHERE  a.cTranStat = '2'" +
                    " AND a.cTLMStatx = '0'" +
                    " AND LEFT(a.sQMatchNo, 2) = 'CI'" +
                    " AND a.dAppliedx >= " + SQLUtil.toSQL(START_DATE) +
                    " AND a.dAppliedx <= " + SQLUtil.toSQL(MiscUtil.dateAdd(poGRider.getServerDate(), DELAY)) + 
                    " AND a.sTransNox NOT IN (" +
                        "SELECT sReferNox FROM Call_Outgoing" +
                        " WHERE sSourceCd = " + SQLUtil.toSQL(SOURCECD) + ")" + 
                " HAVING ISNULL(c.sTransNox)" +
                " ORDER BY a.dAppliedx";
    }
}
