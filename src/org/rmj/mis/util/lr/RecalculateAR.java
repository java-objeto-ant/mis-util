package org.rmj.mis.util.lr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.constants.AccountStatus;

public class RecalculateAR {
    GRider oApp;
    ResultSet oRS; //active accounts
    ResultSet oRSx; //account master information
    ResultSet oRSz; //account ledger information
    String sMessage;
    
    final DecimalFormat DECIMAL = new DecimalFormat("#.##");
    
    public RecalculateAR(GRider foApp){
        oApp = foApp;
        
        sMessage = "";
    }
    
    public boolean Recalc(){
        double lnLastPaym, lnPaymTotl, lnPenTotlx;
        double lnRebTotlx, lnDebtTotl, lnCredTotl;
        double lnAmtDuexx, lnABalance, lnDownTotl;
        double lnCashTotl, lnDelayAvg, lnDelayxxx;
        int lnEntryNox, lnActTermx;
        String ldLastPaym;
        
        try {
            if(oApp == null){
                sMessage = "Application driver is not set.";
                return false;
            }
            
            //get all active AR accounts
            String lsSQL = "SELECT sAcctNmbr FROM MC_AR_Master" +
                            " WHERE cAcctStat = '0'";
            oRS = oApp.executeQuery(lsSQL); //active accounts
            
            while (oRS.next()){
                //get the master info of AR account
                lsSQL = "SELECT * FROM MC_AR_Master" +
                            " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRS.getString(("sAcctNmbr")));
                oRSx = oApp.executeQuery(lsSQL); //account master information
                
                if (oRSx.next()){
                    lsSQL = "SELECT * FROM MC_AR_Ledger" +
                            " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRSx.getString("sAcctNmbr")) +
                            " ORDER BY dTransact" +
                                ", nEntryNox";
                    
                    oRSz = oApp.executeQuery(lsSQL); //account ledger information
                    
                    lnEntryNox = 0;
                    lnPaymTotl = 0.00;
                    lnPenTotlx = 0.00;
                    lnRebTotlx = 0.00;
                    lnDebtTotl = 0.00;
                    lnCredTotl = 0.00;
                    lnDownTotl = 0.00;
                    lnCashTotl = 0.00;
                    lnDelayxxx = 0.00;
                    lnDelayAvg = 0.00;
                    lnLastPaym = 0.00;
                    lnABalance = oRSx.getDouble("nGrossPrc");
                    ldLastPaym = "";
                    
                    while (oRSz.next()){
                        lnEntryNox += 1;
                        
                        switch (oRSz.getString("cTranType")){
                            case "p": //payment
                                lnPaymTotl = lnPaymTotl + oRSz.getDouble("nTranAmtx");
                                lnABalance = lnABalance - oRSz.getDouble("nTranAmtx") - oRSz.getDouble("nRebatesx");
                                lnLastPaym = oRSz.getDouble("nTranAmtx") + oRSz.getDouble("nRebatesx") + oRSz.getDouble("nOthersxx");
                                ldLastPaym = oRSz.getString("dTransact");
                                break;
                            case "d": //down payment
                                lnDownTotl = lnDownTotl + oRSz.getDouble("nTranAmtx");
                                lnABalance = lnABalance - oRSz.getDouble("nTranAmtx");
                                break;
                            case "c": //credit memo
                                lnCredTotl = lnCredTotl + oRSz.getDouble("nTranAmtx");
                                lnABalance = lnABalance - oRSz.getDouble("nTranAmtx");
                                ldLastPaym = oRSz.getString("dTransact");
                                break;
                            case "m": //debit memo
                                lnDebtTotl = lnDebtTotl + oRSz.getDouble("nDebitAmt");
                                lnABalance = lnABalance + oRSz.getDouble("nTranAmtx");
                                break;
                            case "b": //cash balance
                                lnCashTotl = lnCashTotl + oRSz.getDouble("nTranAmtx");
                                lnABalance = lnABalance - oRSz.getDouble("nTranAmtx");
                                lnLastPaym = oRSz.getDouble("nTranAmtx") + oRSz.getDouble("nRebatesx") + oRSz.getDouble("nOthersxx");
                                ldLastPaym = oRSz.getString("dTransact");
                        } //switch (oRSz.getString("cTranType")){
                        
                        lnRebTotlx = lnRebTotlx + oRSz.getDouble("nRebatesx");
                        lnPenTotlx = lnPenTotlx + oRSz.getDouble("nOthersxx");

                        lnActTermx = getMonthTerm(oRSx.getDate("dFirstPay"), oRSz.getDate("dTransact"));

                        //kalyptus - 2020.05.29 11:03pm
                        //Freeze a certain number of months from our account...
                        lnActTermx = lnActTermx - getFreezeMonth(oRSz.getString("sAcctNmbr"), oRSz.getDate("dTransact"));

                        lnAmtDuexx = lnActTermx * oRSx.getDouble("nMonAmort") + oRSx.getDouble("nDownPaym") + oRSx.getDouble("nCashBalx");
                        lnAmtDuexx = lnAmtDuexx - lnPaymTotl - lnDownTotl - lnCashTotl + lnDebtTotl - lnCredTotl - lnRebTotlx;
                        if (lnAmtDuexx < 0) lnAmtDuexx = 0;
                        
                        if (oRSx.getDouble("nMonAmort") > 0.00)
                            lnDelayxxx =  Double.valueOf(DECIMAL.format(lnAmtDuexx / oRSx.getDouble("nMonAmort")));
                        else
                            if (oRSx.getDate("dDueDatex").before(oRSz.getDate("dTransact"))) lnDelayxxx = 1;
      
                    } //while (oRSz.next()){
                    
                    MiscUtil.close(oRSz);
                    
                    //compute base on this date
                    if (oApp.getServerDate().after(oRSx.getDate("dDueDatex"))){
                        lnActTermx = getMonthTerm(oRSx.getDate("dFirstPay"), oRSx.getDate("dDueDatex"));
   
                        //kalyptus - 2020.05.29 11:03pm
                        //Freeze a certain number of months from our account...
                        lnActTermx = lnActTermx - getFreezeMonth(oRSx.getString("sAcctNmbr"), oRSx.getDate("dDueDatex"));
                    } else{
                        lnActTermx = getMonthTerm(oRSx.getDate("dFirstPay"), oApp.getServerDate());
   
                        //kalyptus - 2020.05.29 11:03pm
                       //Freeze a certain number of months from our account...
                        lnActTermx = lnActTermx - getFreezeMonth(oRSx.getString("sAcctNmbr"), oApp.getServerDate());
                    }
                    
                    lnAmtDuexx = lnActTermx * oRSx.getDouble("nMonAmort") + oRSx.getDouble("nDownPaym") + oRSx.getDouble("nCashBalx");
                    lnAmtDuexx = lnAmtDuexx - lnPaymTotl - lnDownTotl - lnCashTotl + lnDebtTotl - lnCredTotl - lnRebTotlx;
                    
                    lsSQL = "";
                    if (lnABalance <= 0.00){
                        lnDelayAvg = getAveDelay(oApp.getServerDate());
                        
                        lsSQL += "cAcctStat = '1'";
                                
                        if (ldLastPaym.isEmpty()){
                            lsSQL += ", dClosedxx = NULL";
                        } else {
                            lsSQL += ", dClosedxx = " + SQLUtil.toSQL(ldLastPaym);
                        }
                        
                        lsSQL += ", cRatingxx = " + SQLUtil.toSQL(getRating(lnDelayAvg, oRSx.getString("cRatingxx"), oRSx.getInt("nAcctTerm")));
                    } else {
                        if (oRSx.getString("cAcctStat").equals(AccountStatus.ACCOUNT_CLOSED)){
                            lsSQL += "cAcctStat = '0'" +
                                     ", dClosedxx = NULL";
                        }    
                        
                        if (oRSx.getDouble("nMonAmort") > 0.00) {
                            lnDelayAvg =  Double.valueOf(DECIMAL.format(lnAmtDuexx / oRSx.getDouble("nMonAmort")));
                        }
                    }
                    
                    if (lsSQL.isEmpty()){
                        lsSQL = "UPDATE MC_AR_Master SET ";
                    } else {
                        lsSQL = "UPDATE MC_AR_Master SET " + lsSQL + ", ";
                    }
                    
                    
                    lsSQL += "nLastPaym = " + lnLastPaym;
                    
                    if (CommonUtils.isDate(ldLastPaym, SQLUtil.FORMAT_SHORT_DATE)){
                        lsSQL += ", dLastPaym = " + SQLUtil.toSQL(ldLastPaym);
                    } else {
                        lsSQL += ", dLastPaym = NULL";
                    }
                    
                    lsSQL += ", nPaymTotl = " + lnPaymTotl +
                             ", nPenTotlx = " + lnPenTotlx +
                             ", nRebTotlx = " + lnRebTotlx +
                             ", nDebtTotl = " + lnDebtTotl +
                             ", nCredTotl = " + lnCredTotl +
                             ", nAmtDuexx = " + lnAmtDuexx +
                             ", nABalance = " + lnABalance +
                             ", nDownTotl = " + lnDownTotl +
                             ", nCashTotl = " + lnCashTotl +
                             ", nDelayAvg = " + lnDelayAvg +
                             ", nLedgerNo = " + lnEntryNox +
                             ", dModified = " + SQLUtil.toSQL(oApp.getServerDate());
                    
                    lsSQL += " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRSx.getString("sAcctNmbr"));
                    
                    oApp.executeQuery(lsSQL, "MC_AR_Master", oApp.getBranchCode(), "");
                }//if (oRSx.next()){
            }
            
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            sMessage = ex.getMessage();
            return false;
        }
    }
    
    private double getAveDelay(Date fdDate){
        try {
            //Pastdue account's delay is based on the past due month
            if (Integer.parseInt(SQLUtil.dateFormat(fdDate, "yyyyMM")) >
                    Integer.parseInt(SQLUtil.dateFormat(oRSx.getDate("dDueDatex"), "yyyyMM"))){
                return Double.valueOf(CommonUtils.monthDiff(SQLUtil.dateFormat(oRSx.getDate("dDueDatex"), SQLUtil.FORMAT_SHORT_DATE), SQLUtil.dateFormat(fdDate, SQLUtil.FORMAT_SHORT_DATE)));
            }
            
            if (oRSx.getInt("nAcctTerm") == 0) return 0.00;
            
            String lsSQL = "SELECT" +
                                "  dTransact" +
                                ", nABalance" +
                             " FROM MC_AR_Ledger" +
                             " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRSx.getString("sAcctNmbr"));
            
            ResultSet loRS = oApp.executeQuery(lsSQL);
            
            int [] lanDayMon = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            
            double lnDelayxxx = 0.00;
            double lnTotDelay = 0.00;
            double lnTranAmtx = 0.00;
            Date ldTranDate = oRSx.getDate("dFirstPay");
            int lnDayOfMon = Integer.valueOf(SQLUtil.dateFormat(ldTranDate, "dd"));
            
            int lnCtr;
            for (lnCtr = 1; lnCtr <= oRSx.getInt("nAcctTerm"); lnCtr++){
                lnTranAmtx = 0.00;
                if (MiscUtil.RecordCount(loRS) != 0){
                    loRS.first();
                    while (CommonUtils.dateDiff(loRS.getDate("dTransact"), ldTranDate) >= 1){
                        lnTranAmtx = oRSx.getDouble("nGrossPrc") - loRS.getDouble("nABalance");
                        
                        if(!loRS.next()) break;
                    }
                    
                    lnDelayxxx = (lnCtr * oRSx.getDouble("nMonAmort") + oRSx.getDouble("nDownPaym") - lnTranAmtx) / oRSx.getDouble("nMonAmort");
                    
                    //kalyptus - 2020.05.29 01:59pm
                    //Remove number of freezed month from the delay
                    lnDelayxxx -= getFreezeMonth(oRSx.getString("sAcctNmbr"), ldTranDate);
                    
                    lnTotDelay += lnDelayxxx;
                    
                    ldTranDate = CommonUtils.dateAdd(ldTranDate, 1);
                    
                    if (lnDayOfMon > Integer.valueOf(SQLUtil.dateFormat(ldTranDate, "dd"))){
                        if (lanDayMon[Integer.parseInt(SQLUtil.dateFormat(ldTranDate, "MM")) - 1] > Integer.valueOf(SQLUtil.dateFormat(ldTranDate, "dd"))){
                            String lsMonth = SQLUtil.dateFormat(ldTranDate, "MM");
                            String lsDay = String.valueOf(lanDayMon[Integer.valueOf(lsMonth) - 1]);
                            String lsYear = SQLUtil.dateFormat(ldTranDate, "yyyy");
                            
                            ldTranDate = SQLUtil.toDate(lsYear + "-" + lsMonth + "-" + lsDay, SQLUtil.FORMAT_SHORT_DATE);
                        }
                    }
                }
            }
            
            if (lnCtr > 1) lnCtr -= 1;
            
            lnTotDelay = Double.valueOf(CommonUtils.NumberFormat(lnTotDelay / lnCtr, "##.00"));
            
            if (lnTotDelay < 0.00)
                return 0.00;
            else
                return lnTotDelay * (-1);
        } catch (SQLException ex) {
            Logger.getLogger(RecalculateAR.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        return -100;
    }
    
    private int getFreezeMonth(String fsAcctNmbr, Date fdTransact){
        String lsSQL = "SELECT sAcctNmbr, b.*" +
                        " FROM MC_AR_Master a" +
                            " LEFT JOIN Branch_Lockdown_History b ON a.sBranchCd = b.sBranchCD" +
                        " WHERE sAcctNmbr = " + SQLUtil.toSQL(fsAcctNmbr) +
                          " AND b.dDateFrom > a.dFirstPay" +
                          " AND b.dDateThru < " + SQLUtil.toSQL(fdTransact);
        
        ResultSet loRS = oApp.executeQuery(lsSQL);
        
        int lnCtr = 0;
        
        try {
            while(loRS.next()){
                lnCtr += loRS.getInt("nMonthxxx");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RecalculateAR.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        return lnCtr;
    }
    
    private int getMonthTerm(Date fdFirstPay, Date fdTransact){        
        int lnTerm = (int) CommonUtils.monthDiff(SQLUtil.dateFormat(fdFirstPay, SQLUtil.FORMAT_SHORT_DATE), 
                                                    SQLUtil.dateFormat(fdTransact, SQLUtil.FORMAT_SHORT_DATE)) + 1;       
        
        int lnFPayDayx = Integer.parseInt(SQLUtil.dateFormat(fdFirstPay, "dd"));
        int lnTransact = Integer.parseInt(SQLUtil.dateFormat(fdTransact, "dd"));
        
        if (lnFPayDayx > lnTransact)
            return lnTerm - 1;
        else 
            return lnTerm;
    }
    
    private String getRating(double fnDelay, String fcRating, int fnActTerm){
        if (fnActTerm == 0){
            return "n";
        }else if (fcRating.equals("1")){
            //Blacklisted account are account that are impounded...
            return fcRating;
        }else if (fnDelay <= 0){
            fnDelay = fnDelay * (-1);
            
            if (fnDelay > 0.5)
                return "g";
            else 
                return "x";
        }else {
            if (fnDelay < 6)
                return "f";
            else if (fnDelay < 12)
                return "p";
            else
                return "b";
        }
    }
    
    public String getMessage(){
        return sMessage;
    }
}
