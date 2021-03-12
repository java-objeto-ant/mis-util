package org.rmj.mis.util.sms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;

/**
 * Follow Up Payment SMS Processor
 * 
 * @author mac
 * @since 2021.03.03
 */
public class FUPYMT implements iSMS{
    private final String MASTABLE = "HotLine_Outgoing";
    private final String SOURCECD = "remP";
    private final String TEMPLATE = "M001130009";
    private final int DAYSB4 = 3;
    
    GRiderX oApp;
    String psMessage;
    String psTemplate;
    int pnRow;
    int pnInvalid;
    
    ArrayList<String> loTemplate;
    
    @Override
    public void setGRider(GRiderX foValue) {
        oApp = foValue;
        
        loTemplate = new ArrayList<>();
        loTemplate.add("<sFrstName>");
        loTemplate.add("<nFirstPay>");
        loTemplate.add("<xMgrFName>");
        loTemplate.add("<xMgrTelNo>");
        loTemplate.add("<xMgrCelNo>");
        
        pnRow = 0;
        pnInvalid = 0;
    }

    @Override
    public boolean Process() {
        try {
            if (oApp == null){
                setMessage("Application Driver is not set.");
                return false;
            }
            
            //get message template
            String lsSQL = "SELECT sFormatxx FROM Hotline_Template WHERE sHotlneCd = " + SQLUtil.toSQL(TEMPLATE);
            
            ResultSet loRS = oApp.executeQuery(lsSQL);
            if (!loRS.next()){
                psMessage = "Message template not found.";
                return false;
            }
            psTemplate = loRS.getString("sFormatxx");
            //end - get message template
            
            lsSQL = getSQ_Master();
            //if client query is not empty then continue loading
            if (lsSQL.equals("")) return false;
            
            loRS = oApp.executeQuery(lsSQL);
            
            String lsMessagex;
            String lsMobileNo;
            
            oApp.beginTrans();
            while(loRS.next()){
                lsMessagex = getIndvidualMessage(loRS.getString("sFrstName"), loRS.getString("dFirstPay"), loRS.getString("xMgrFName"), loRS.getString("xMgrTelNo"), loRS.getString("xMgrCelNo"));
                lsMobileNo = CommonUtils.fixMobileNo(loRS.getString("sMobileNo"));
                
                if (!lsMobileNo.isEmpty()){
                    lsSQL = "INSERT INTO " + MASTABLE + " SET" + 
                            "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode(MASTABLE, "sTransNox", true, oApp.getConnection(), oApp.getBranchCode())) +
                            ", dTransact = " + SQLUtil.toSQL(oApp.getServerDate()) +
                            ", sDivision = 'CSS'" +
                            ", sMobileNo = " + SQLUtil.toSQL(lsMobileNo) + 
                            ", sMessagex = " + SQLUtil.toSQL(lsMessagex) + 
                            ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork(lsMobileNo)) +
                            ", dDueUntil = " + SQLUtil.toSQL(CommonUtils.dateAdd(oApp.getServerDate(), DAYSB4)) +
                            ", cSendStat = '0'" + 
                            ", nPriority = 4" +
                            ", nNoRetryx = 0" + 
                            ", sUDHeader = ''" + 
                            ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sAcctNmbr")) +
                            ", sSourceCd = " + SQLUtil.toSQL(SOURCECD) + 
                            ", cTranStat = '0'" +
                            ", sModified = " + SQLUtil.toSQL(oApp.getUserID()) + 
                            ", dModified = " + SQLUtil.toSQL(oApp.getServerDate());
               
                    if (oApp.executeUpdate(lsSQL) <= 0){
                        oApp.rollbackTrans();
                        psMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                        return false;
                    }
                    
                    pnRow += 1;
                } else
                    pnInvalid += 1; 
                
            }
            oApp.commitTrans();
        } catch (SQLException ex) {
            oApp.rollbackTrans();
            psMessage = ex.getMessage();
            return false;
        }
        
        return true;
    }
    
    @Override
    public int getItemCount() {
        return pnRow;
    }
    
    @Override
    public int getInvalid() {
        return pnInvalid;
    }

    @Override
    public void setMessage(String fsValue) {
        psMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return psMessage;
    }
    
    private String getIndvidualMessage(String fsFrstName, String fdFirstPay, String fsMgrFName, 
            String fsMgrTelNo, String fsMgrCelNo){
        
        if (psTemplate.isEmpty()) return "";
        
        String lsValue = psTemplate;
        int lnDay;
        
        for (int lnCtr = 0; lnCtr <= loTemplate.size()-1; lnCtr++){
            switch(loTemplate.get(lnCtr)){
                case "<sFrstName>":
                    lsValue = lsValue.replace(loTemplate.get(lnCtr), fsFrstName);
                    break;
                case "<nFirstPay>":
                    lnDay = CommonUtils.getDateDay(SQLUtil.toDate(fdFirstPay, SQLUtil.FORMAT_SHORT_DATE));
                    
                    if (CommonUtils.isBetween(lnDay, 1, DAYSB4)){
                        if (lnDay < CommonUtils.getDateDay(oApp.getServerDate())) lnDay = 0;
                    }
                    
                    if (lnDay == 0)
                        lsValue = lsValue.replace(loTemplate.get(lnCtr), SQLUtil.dateFormat(CommonUtils.dateAdd(SQLUtil.toDate(fdFirstPay, SQLUtil.FORMAT_SHORT_DATE), Calendar.MONTH, 1), "MMM") + ". " + lnDay);
                    else
                        lsValue = lsValue.replace(loTemplate.get(lnCtr), SQLUtil.dateFormat(oApp.getServerDate(), "MMM") + ". " + lnDay);
                    
                    break;
                case "<xMgrFName>":
                    if (fsMgrTelNo.trim().isEmpty() && fsMgrCelNo.trim().isEmpty())
                        lsValue = lsValue.replace(" or call Mr./Ms. <xMgrFName> at <xMgrTelNo> or <xMgrCelNo>", "");
                    else
                        lsValue = lsValue.replace(loTemplate.get(lnCtr), fsMgrFName);
                    break;
                case "<xMgrTelNo>":
                    if (!fsMgrTelNo.trim().isEmpty() && fsMgrCelNo.trim().isEmpty())
                        lsValue = lsValue.replace(loTemplate.get(lnCtr) + " or ", fsMgrTelNo);
                    else if(fsMgrTelNo.trim().isEmpty())
                        lsValue = lsValue.replace(loTemplate.get(lnCtr) + " or ", "");
                    else
                        lsValue = lsValue.replace(loTemplate.get(lnCtr), fsMgrTelNo);
                    break;
                case "<xMgrCelNo>":
                    lsValue = lsValue.replace(loTemplate.get(lnCtr), fsMgrCelNo);                    
                    break;
            }   
        }
        
        return lsValue;
    }
    
    private String getSQ_Master(){
        String lsSQL = "SELECT" +
                            " dTransact" +
                         " FROM HotLine_Outgoing" +
                         " WHERE sSourceCd = " + SQLUtil.toSQL(SOURCECD) +
                         " ORDER BY dTransact DESC" +
                         " LIMIT 1";
        
        ResultSet loRS = oApp.executeQuery(lsSQL);
        
        int lnCtr;
        int lnDaysFrom;
        Date ldStart;
        String lsIncDay = "";
        
        try {
            if (!loRS.next())
                lnDaysFrom = 0;
            else{
                if (CommonUtils.dateDiff(SQLUtil.toDate(loRS.getString("dTransact"), SQLUtil.FORMAT_SHORT_DATE), 
                                        SQLUtil.toDate(oApp.getServerDate().toString(), SQLUtil.FORMAT_SHORT_DATE)) == 0){
                    psMessage = "Text reminders are already created for this day.";
                    return "";
                } else {
                    lnDaysFrom = DAYSB4 - ((int) CommonUtils.dateDiff(SQLUtil.toDate(oApp.getServerDate().toString(), SQLUtil.FORMAT_SHORT_DATE), 
                                        SQLUtil.toDate(loRS.getString("dTransact"), SQLUtil.FORMAT_SHORT_DATE)) - 1);
                    if (lnDaysFrom <= 0) lnDaysFrom = 1;
                }
            }
            
            if (lnDaysFrom == 0 || lnDaysFrom == DAYSB4){
                ldStart = CommonUtils.dateAdd(oApp.getServerDate(), DAYSB4);
                
                if (CommonUtils.getDateMonth(ldStart) != CommonUtils.getDateMonth(CommonUtils.dateAdd(ldStart, 1))){
                    //if date is last day of the month, always include up to 31
                    for(lnCtr = CommonUtils.getDateDay(ldStart); lnCtr <= 31; lnCtr++){
                        lsIncDay = lsIncDay + ", " + lnCtr;
                    }
                    lsIncDay = "DAY(a.dFirstPay) IN (" + lsIncDay.substring(1) + ")";
                } else {
                    lsIncDay = "DAY(a.dFirstPay) = " + CommonUtils.getDateDay(ldStart);
                }
            } else {
                //check first the beginning date of the criteria
                ldStart = CommonUtils.dateAdd(oApp.getServerDate(), lnDaysFrom);
                
                if (CommonUtils.getDateMonth(ldStart) != CommonUtils.getDateMonth(CommonUtils.dateAdd(ldStart, 1)) ||
                    CommonUtils.getDateMonth(ldStart) != CommonUtils.getDateMonth(CommonUtils.dateAdd(oApp.getServerDate(), DAYSB4))){
                    // if date is last day of the month or the end date is next month, always include up to 31
                    for(lnCtr = CommonUtils.getDateDay(ldStart); lnCtr <= 31; lnCtr++){
                        lsIncDay = lsIncDay + ", " + lnCtr;
                    }
                    
                    for (lnCtr = CommonUtils.getDateDay(CommonUtils.dateAdd(oApp.getServerDate(), DAYSB4)); lnCtr >= 1; lnCtr--){
                        lsIncDay = ", " + lnCtr + lsIncDay;
                    }
                    lsIncDay = "DAY(a.dFirstPay) IN (" + lsIncDay.substring(1) + ")";
                } else {
                    for(lnCtr = CommonUtils.getDateDay(ldStart); lnCtr <= CommonUtils.getDateDay(CommonUtils.dateAdd(oApp.getServerDate(), DAYSB4)); lnCtr++){
                        lsIncDay = lsIncDay + ", " + lnCtr;
                    }
                    lsIncDay = "DAY(a.dFirstPay) IN (" + lsIncDay.substring(1) + ")";
                }
            }
            
            lsSQL = "SELECT" +
                        "  sAcctNmbr" +
                        ", sLastName" +
                        ", sFrstName" +
                        ", sPhoneNox" +
                        ", sMobileNo" +
                        ", nMonAmort" +
                        ", IFNULL(xMgrFName, '') xMgrFName" +
                        ", IFNULL(xMgrTelNo, '') xMgrTelNo" +
                        ", IFNULL(xMgrCelNo, '') xMgrCelNo" +
                        ", COUNT(sAcctNmbr) xNoClient" +
                        ", dFirstPay" +
                    " FROM ( " +
                        "SELECT" +
                            "  a.sAcctNmbr" +
                            ", b.sLastName" +
                            ", b.sFrstName" +
                            ", b.sPhoneNox" +
                            ", b.sMobileNo" +
                            ", a.nMonAmort" +
                            ", f.sFrstName xMgrFName" +
                            ", e.sTelNumbr xMgrTelNo" +
                            ", f.sPhoneNox xMgrCelNo" +
                            ", a.nPaymTotl" +
                            ", a.dFirstPay" +
                        " FROM MC_AR_Master a" +
                            ", Client_Master b" +
                            ", Route_Area c" +
                            ", Employee_Master001 d" +
                            ", Branch e" +
                            ", Client_Master f" +
                        " WHERE a.sClientID = b.sClientID" +
                            " AND a.sRouteIDx = c.sRouteIDx" +
                            " AND c.sManagrID = d.sEmployID" +
                            " AND c.sBranchCd = e.sBranchCd" +
                            " AND d.sEmployID = f.sClientID" +
                            " AND a.cActivexx = " + SQLUtil.toSQL(1) +
                            " AND a.cAcctStat = " + SQLUtil.toSQL(0) +
                            " AND a.nAcctTerm > 0" +
                            " AND a.nDelayAvg > -1" +
                            " AND a.nDelayAvg <= 0" +
                            " AND DATE_FORMAT(a.dLastPaym, " + SQLUtil.toSQL("%Y%m") + ") <> " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND DATE_FORMAT(a.dFirstPay, " + SQLUtil.toSQL("%Y%m") + ") <= " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND " + lsIncDay +
                            " AND (LENGTH(b.sMobileNo) >= 11 OR LENGTH(b.sPhoneNox) >= 11)" +
                        " UNION SELECT" +
                            "  a.sAcctNmbr" +
                            ", b.sLastName" +
                            ", b.sFrstName" +
                            ", b.sPhoneNox" +
                            ", b.sMobileNo" +
                            ", a.nMonAmort" +
                            ", f.sFrstName xMgrFName" +
                            ", e.sTelNumbr xMgrTelNo" +
                            ", f.sPhoneNox xMgrCelNo" +
                            ", a.nPaymTotl" +
                            ", a.dFirstPay" +
                        " FROM LR_Master a" +
                            ", Client_Master b" +
                            ", Route_Area c" +
                            ", Employee_Master001 d" +
                            ", Branch e" +
                            ", Client_Master f" +
                        " WHERE a.sClientID = b.sClientID" +
                            " AND a.sRouteIDx = c.sRouteIDx" +
                            " AND c.sManagrID = d.sEmployID" +
                            " AND c.sBranchCd = e.sBranchCd" +
                            " AND d.sEmployID = f.sClientID" +
                            " AND a.cActivexx = " + SQLUtil.toSQL(1) +
                            " AND a.cAcctStat = " + SQLUtil.toSQL(0) +
                            " AND a.nAcctTerm > 0" +
                            " AND a.nDelayAvg > -1" +
                            " AND a.nDelayAvg <= 0" +
                            " AND DATE_FORMAT(a.dLastPaym, " + SQLUtil.toSQL("%Y%m") + ") <> " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND DATE_FORMAT(a.dFirstPay, " + SQLUtil.toSQL("%Y%m") + ") <= " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND " + lsIncDay +
                            " AND (LENGTH(b.sMobileNo) >= 11 OR LENGTH(b.sPhoneNox) >= 11)" +
                        " UNION SELECT" +
                            "  a.sAcctNmbr" +
                            ", '' sLastName" +
                            ", '' sFrstName" +
                            ", '' sPhoneNox" +
                            ", '' sMobileNo" +
                            ", a.nMonAmort" +
                            ", '' xMgrFName" +
                            ", '' xMgrTelNo" +
                            ", '' xMgrCelNo" +
                            ", SUM(b.nAmountxx + b.nPenaltyx) nPaymTotl" +
                            ", a.dFirstPay" +
                        " FROM LR_Master a" +
                            ", LR_Payment_Master_PR b" +
                            ", Checks_Received c" +
                        " WHERE a.sAcctNmbr = b.sAcctNmbr" +
                            " AND b.sTransNox = c.sReferNox" +
                            " AND c.sSourceCd = " + SQLUtil.toSQL("PRec") +
                            " AND a.cActivexx = " + SQLUtil.toSQL(1) +
                            " AND a.cAcctStat = " + SQLUtil.toSQL(0) +
                            " AND a.nAcctTerm > 0" +
                            " AND a.nDelayAvg > -1" +
                            " AND a.nDelayAvg <= 0" +
                            " AND DATE_FORMAT(a.dLastPaym, " + SQLUtil.toSQL("%Y%m") + ") <> " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND DATE_FORMAT(a.dFirstPay, " + SQLUtil.toSQL("%Y%m") + ") <= " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND " + lsIncDay +
                            " AND b.cPostedxx = " + SQLUtil.toSQL(0) +
                            " AND DATE_FORMAT(c.dCheckDte, " + SQLUtil.toSQL("%Y%m") + ") <= " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                        " UNION SELECT" +
                            "  a.sAcctNmbr" +
                            ", '' sLastName" +
                            ", '' sFrstName" +
                            ", '' sPhoneNox" +
                            ", '' sMobileNo" +
                            ", a.nMonAmort" +
                            ", '' xMgrFName" +
                            ", '' xMgrTelNo" +
                            ", '' xMgrCelNo" +
                            ", SUM(b.nTranAmtx + b.nDiscount) nPaymTotl" +
                            ", a.dFirstPay" +
                        " FROM MC_AR_Master a" +
                            ", Provisionary_Receipt_Master b" +
                            ", Checks_Received c" +
                        " WHERE a.sAcctNmbr = b.sAcctNmbr" +
                            " AND b.sTransNox = c.sReferNox" +
                            " AND c.sSourceCd = " + SQLUtil.toSQL("PRec") +
                            " AND a.cActivexx = " + SQLUtil.toSQL(1) +
                            " AND a.cAcctStat = " + SQLUtil.toSQL(0) +
                            " AND a.nAcctTerm > 0" +
                            " AND a.nDelayAvg > -1" +
                            " AND a.nDelayAvg <= 0" +
                            " AND DATE_FORMAT(a.dLastPaym, " + SQLUtil.toSQL("%Y%m") + ") <> " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND DATE_FORMAT(a.dFirstPay, " + SQLUtil.toSQL("%Y%m") + ") <= " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                            " AND " + lsIncDay +
                            " AND b.cTranStat = " + SQLUtil.toSQL(0) +
                            " AND DATE_FORMAT(c.dCheckDte, " + SQLUtil.toSQL("%Y%m") + ") <= " + SQLUtil.toSQL(SQLUtil.dateFormat(ldStart, "yyyyMM")) +
                        " GROUP BY a.sAcctNmbr" +
                        " HAVING nPaymTotl >= a.nMonAmort" +
                    " ) xSource" +
                    " GROUP BY sAcctNmbr" +
                    " HAVING IFNULL(sAcctNmbr, '') <> '' AND xNoClient = 1 ";
        } catch (SQLException ex) {
            psMessage = ex.getMessage();
        }
        
        return lsSQL;
    }
}
