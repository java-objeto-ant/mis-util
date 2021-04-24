package org.rmj.mis.util.lr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;

public class RecalculateAR {
    GRider oApp;
    ResultSet oRS;
    ResultSet oRSx;
    String sMessage;
    
    public RecalculateAR(GRider foApp){
        oApp = foApp;
        
        sMessage = "";
    }
    
    public boolean Recalc(){
        try {
            if(oApp == null){
                sMessage = "Application driver is not set.";
                return false;
            }
            
            //get all active AR accounts
            String lsSQL = "SELECT sAcctNmbr FROM MC_AR_Master" +
                            " WHERE cAcctStat = '0'";
            oRS = oApp.executeQuery(lsSQL);
            
            while (oRS.next()){
                //get the master info of AR account
                lsSQL = "SELECT * FROM MC_AR_Master" +
                            " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRS.getString(("sAcctNmbr")));
                oRSx = oApp.executeQuery(lsSQL);
                
                if (oRSx.next()){
                
                }
            }
            
            return true;
        } catch (SQLException ex) {
            sMessage = ex.getMessage();
            return false;
        }
    }
    
    private double getAveDelay(Date fdDate){
        try {
            //Pastdue account's delay is based on the past due month
            if (Integer.parseInt(SQLUtil.dateFormat(fdDate, "yyyyMM")) >
                    Integer.parseInt(SQLUtil.dateFormat(oRS.getDate("dDueDatex"), "yyyyMM"))){
                return Double.valueOf(CommonUtils.monthDiff(SQLUtil.dateFormat(oRS.getDate("dDueDatex"), SQLUtil.FORMAT_SHORT_DATE), SQLUtil.dateFormat(fdDate, SQLUtil.FORMAT_SHORT_DATE)));
            }
            
            if (oRS.getInt("nAcctTerm") == 0) return 0.00;
            
            String lsSQL = "SELECT" +
                                "  dTransact" +
                                ", nABalance" +
                             " FROM MC_AR_Ledger" +
                             " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRS.getString("sAcctNmbr"));
            
            ResultSet loRS = oApp.executeQuery(lsSQL);
            
            int [] lanDayMon = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            
            double lnDelayxxx = 0.00;
            double lnTotDelay = 0.00;
            double lnTranAmtx = 0.00;
            Date ldTranDate = oRS.getDate("dFirstPay");
            int lnDayOfMon = Integer.valueOf(SQLUtil.dateFormat(ldTranDate, "dd"));
            
            int lnCtr;
            for (lnCtr = 1; lnCtr <= oRS.getInt("nAcctTerm"); lnCtr++){
                lnTranAmtx = 0.00;
                if (MiscUtil.RecordCount(loRS) != 0){
                    loRS.first();
                    while (CommonUtils.dateDiff(loRS.getDate("dTransact"), ldTranDate) >= 1){
                        lnTranAmtx = oRS.getDouble("nGrossPrc") - loRS.getDouble("nABalance");
                        
                        if(!loRS.next()) break;
                    }
                    
                    lnDelayxxx = (lnCtr * oRS.getDouble("nMonAmort") + oRS.getDouble("nDownPaym") - lnTranAmtx) / oRS.getDouble("nMonAmort");
                    
                    //kalyptus - 2020.05.29 01:59pm
                    //Remove number of freezed month from the delay
                    lnDelayxxx -= getFreezeMonth(oRS.getString("sAcctNmbr"), ldTranDate);
                    
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
