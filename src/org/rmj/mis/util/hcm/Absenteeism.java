package org.rmj.mis.util.hcm;

import org.rmj.mis.util.factory.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class Absenteeism implements UtilityValidator{
    private final String MOBILE_HCM = "09178215721";
    private final String MOBILE_JM = "09178209234";
    
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
        try {
            //get all the active employees
            String lsSQL = getSQ_Master();
            ResultSet loMaster = instance.executeQuery(lsSQL);
            
            ResultSet loTimesheet;
            ResultSet loMobile;
            
            int lnAbsent;
            
            int lnCtr = 0;
            String lsMessage;
            while (loMaster.next()){
                lnAbsent = 0;
                
                //get the employee timesheet
                lsSQL = MiscUtil.addCondition(getSQ_Timesheet(), "a.sEmployID = " + SQLUtil.toSQL(loMaster.getString("sEmployID")));
                loTimesheet = instance.executeQuery(lsSQL);
                
                //count consecutive absences
                while (loTimesheet.next()){
                    if (loTimesheet.getString("cDeductxx").equals("1")){
                        if (loTimesheet.getString("cOnLeavex").equals("0") &&
                            loTimesheet.getString("cOnOBTrip").equals("0") &&
                            loTimesheet.getString("cOnOBLogx").equals("0") &&
                            loTimesheet.getString("cEmpLogxx").equals("0")){
                            lnAbsent += 1;
                        }
                    } else break;
                }
                
                //2 inform BH/DH
                //5 inform HCM
                if (lnAbsent >= 2) {                    
                    //get the BH/DH number
                    if ("015;036;038".contains(loMaster.getString("sDeptIDxx"))){//sales
                        lsSQL = MiscUtil.addCondition(getSQ_Mobile(), "a.sBranchCd = " + SQLUtil.toSQL(loMaster.getString("sBranchCd")) +
                                                                        " AND a.sDeptIDxx = " + SQLUtil.toSQL(loMaster.getString("sDeptIDxx")));
                        
                        lsMessage = "Mr./Ms. " + loMaster.getString("sCompnyNm") + "(" + loMaster.getString("sMobileNo") + ")" + " of " + loMaster.getString("sBranchNm") + 
                                " has " + lnAbsent + " consecutive absences.\n\n-GUANZON";
                    } else { //non sales
                        lsSQL = MiscUtil.addCondition(getSQ_Mobile(), "a.sDeptIDxx = " + SQLUtil.toSQL(loMaster.getString("sDeptIDxx")));
                        
                        lsMessage = "Mr./Ms. " + loMaster.getString("sCompnyNm") + "(" + loMaster.getString("sMobileNo") + ")" + " of " + loMaster.getString("sDeptName") + 
                                " has " + lnAbsent + " consecutive absences.\n\n-GUANZON";
                    }
                    
                    loMobile = instance.executeQuery(lsSQL);
                    
                    //send message to BH/DH
                    if (loMobile.next()){
                        if (!loMobile.getString("sMobileNo").isEmpty())
                            createSMS(loMaster.getString("sEmployID"), loMobile.getString("sMobileNo"), lsMessage);
                    }
                    
                    //send message to HCM
                    if (lnAbsent >= 5) {
                        if (loMaster.getString("sBranchCD").equals("N001") ||
                            loMaster.getString("sBranchCD").equals("A001"))
                            createSMS(loMaster.getString("sEmployID"), MOBILE_JM, lsMessage);
                        else
                            createSMS(loMaster.getString("sEmployID"), MOBILE_HCM, lsMessage);
                    }
                    
                    MiscUtil.close(loMobile);
                }
                
                MiscUtil.close(loTimesheet);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage("SQL Exception...");
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
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sEmployID" +
                    ", b.sCompnyNm" +
                    ", d.sBranchNm" +
                    ", e.sDeptName" +
                    ", c.sPositnNm" +
                    ", a.sBranchCd" +
                    ", a.sEmpLevID" +
                    ", a.sDeptIDxx" +
                    ", a.sPositnID" +
                    ", a.sBranchCd" +
                    ", IF(IFNULL(b.sMobileNo, '') = '', IFNULL(b.sPhoneNox, ''), b.sMobileNo) sMobileNo" +
                " FROM Employee_Master001 a" +
                    " LEFT JOIN Client_Master b" +
                        " ON a.sEmployID = b.sClientID" +
                    " LEFT JOIN Position c" +
                        " ON a.sPositnID = c.sPositnID" +
                    " LEFT JOIN Branch d" +
                        " ON a.sBranchCd = d.sBranchCd" +
                    " LEFT JOIN Department e" +
                        " ON a.sDeptIDxx = e.sDeptIDxx" +
                " WHERE LEFT(a.sBranchCd, 1) IN ('M', 'C')" +
                    " AND a.sEmpLevID IN ('0','1')" +
                    " AND a.cSalTypex <> 'S'" +
                    " AND a.dFiredxxx IS NULL" +
                    " AND a.cRecdStat = '1'" +
                    " AND a.sEmployID <> 'M05009000007'" +
                " HAVING a.sEmployID <> '' AND a.sDeptIDxx <> ''" +
                " ORDER BY a.sBranchCd, a.sDeptIDxx";
    }
    
    private String getSQ_Timesheet(){
        return "SELECT" + 
                    "  a.dTransact" +
                    ", a.cDeductxx" +
                    ", IFNULL(b.sTransNox, '0') cOnLeavex" +
                    ", IFNULL(c.sTransNox, '0') cOnOBTrip" +
                    ", IFNULL(d.sTransNox, '0') cOnOBLogx" +
                    ", IFNULL(e.sEmployID, '0') cEmpLogxx" +
                " FROM Employee_Timesheet a" +
                    " LEFT JOIN Employee_Leave b" + 
                        " ON a.sEmployID = b.sEmployID" + 
                            " AND a.dTransact BETWEEN b.dAppldFrx AND b.dAppldTox" + 
                            " AND b.cTranStat <> '3'" + 
                    " LEFT JOIN Employee_Business_Trip c" + 
                        " ON a.sEmployID = c.sEmployID" + 
                            " AND a.dTransact between c.dAppldFrx and c.dAppldTox" + 
                            " AND c.cTranStat <> '3'" + 
                    " LEFT JOIN Employee_Log_F2S d" + 
                        " ON a.sEmployID = d.sEmployID" + 
                            " AND a.dTransact = d.dTransact" + 
                            " AND d.cTranStat <> '3'" + 
                            " AND d.cIsOBxxxx = '1'" + 
                    " LEFT JOIN Employee_Log e" +
                        " ON a.sEmployID = e.sEmployID" +
                            " AND a.dTransact = e.dTransact" +
                " ORDER BY a.dTransact DESC" +  
                " LIMIT 15";
    }
    
    private String getSQ_Mobile(){
        return "SELECT" +
                    " IF(IFNULL(c.sMobileNo, '') = '', IFNULL(c.sPhoneNox, ''), c.sMobileNo) sMobileNo" +
                " FROM Employee_Master001 a" +
                    " LEFT JOIN Client_Master c" +
			" ON a.sEmployID = c.sClientID" +
                    ", Position b" +
                " WHERE a.sPositnID = b.sPositnID" +
                    " AND b.sPositnID IN ('036', '069', '087', '053', '002', '077', '083', '072', '057', '050', '194','133', '066', '176', '075', '059', '009', '125')" +
                    " AND a.dFiredxxx IS NULL" +
                    " AND a.cRecdStat = '1'" +
                " ORDER BY a.sEmpLevID DESC LIMIT 1";
    }
    
    private boolean createSMS(String fsEmployID, String fsMobileNo, String fsMessagex){
        String lsSQL = "INSERT INTO HotLine_Outgoing SET" +
                        "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("HotLine_Outgoing", "sTransNox", true, instance.getConnection(), "MX01")) +
                        ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) +
                        ", sDivision = 'HCM'" +
                        ", sMobileNo = " + SQLUtil.toSQL(fsMobileNo) +
                        ", sMessagex = " + SQLUtil.toSQL(fsMessagex) +
                        ", cSubscrbr = '0'" + 
                        ", dDueUntil = " + SQLUtil.toSQL(instance.getServerDate()) +
                        ", cSendStat = '0'" +
                        ", nNoRetryx = '0'" +
                        ", sUDHeader = ''" +
                        ", sReferNox = " + SQLUtil.toSQL(fsEmployID) +
                        ", sSourceCd = 'ATTN'" +
                        ", cTranStat = '0'" +
                        ", nPriority = 1" +
                        ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                        ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
        
        return instance.executeUpdate(lsSQL) > 0;
    }
}
