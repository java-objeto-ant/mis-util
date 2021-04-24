package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.mis.util.factory.UtilityValidator;
import org.rmj.mis.util.factory.UtilityValidatorFactory;
import org.rmj.replication.utility.LogWrapper;

public class FixHolyWeek2021 {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("HolyWeek2021", "hcm.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("gRider");
        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        FixHoliday fixHoliday = new FixHoliday(instance, logwrapr);
        if (fixHoliday.Execute()){
            System.out.println("Thank you!");
            logwrapr.info("Thank you!");
            System.exit(0); 
        } else{
            System.out.println(fixHoliday.getMessage());
            logwrapr.info(fixHoliday.getMessage());
            System.exit(1); 
        }
    }
}

class FixHoliday{
    private GRiderX oApp;
    private String sMessage;
    
    private LogWrapper logwrapr;
    
    public FixHoliday(GRiderX foApp, LogWrapper logwrapr){
        oApp = foApp;
        this.logwrapr = logwrapr;
    }
    
    public boolean Execute(){
        if (oApp == null){
            sMessage = "Application driver is not set.";
            return false;
        }
        
        String lsSQL = getSQL_Master();
        ResultSet loRS = oApp.executeQuery(lsSQL);
        int lnCtr;
        
        try {
            oApp.beginTrans();
            
            while (loRS.next()){
                //check if the employee has attendance on the day before the holiday
                if (loRS.getString("dAMInxxxx") != null || loRS.getString("dAMOutxxx") != null ||
                        loRS.getString("dPMInxxxx") != null || loRS.getString("dPMOutxxx") != null){
                    
                    //check if the employee has timesheet record on the day before holiday
                    //if having, update the cAbsentxx to 1 and cDeductxx to 0
                    //if none, insert timesheet record with cAbsentxx to 1 and cDeductxx to 0
                    if (loRS.getString("cAbsentxx") == null){
                        lsSQL = "INSERT INTO Employee_Timesheet SET" +
                                    "  sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    ", dTransact = '2021-03-31'" +
                                    ", cAbsentxx = '0'" +
                                    ", cLeavexxx = '0'" +
                                    ", cHolidayx = '0'" +
                                    ", cDeductxx = '0'" +
                                    ", cRestDayx = '0'" +
                                    ", nTardyxxx = 0" +
                                    ", nOverTime = 0" +
                                    ", nUndrTime = 0" +
                                    ", nUnOffOTx = 0" +
                                    ", nPayRatex = 1" +
                                    ", nOTRatexx = 1" +
                                    ", nNightDif = 0" +
                                    ", nOTNghtDf = 0" +
                                    ", nUnOTNght = 0" +
                                    ", nOrigTard = 0" +
                                    ", nOrigUndr = 0" +
                                    ", nAttEqual = 1" +
                                    ", cInvalidx = '0'";
                        logwrapr.info("INSERT Employee_Timesheet 2021-03-31" + "\t" + loRS.getString("sEmployID"));
                    } else {
                        lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cAbsentxx = '0'" +
                                    ", cDeductxx = '0'" + 
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2021-03-31'";
                        logwrapr.info("UPDATE Employee_Timesheet 2021-03-31" + "\t" + loRS.getString("sEmployID") + "\t" + loRS.getString("cAbsentxx") + "\t" + loRS.getString("cDeductxx"));
                    }
                    
                    if (oApp.executeQuery(lsSQL, "Employee_Timesheet", oApp.getBranchCode(), loRS.getString("sBranchCd")) <= 0){
                        sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                        oApp.rollbackTrans();
                        return false;
                    }
                    //end - check if the employee has timesheet record on the day before holiday
                    
                    //check if the employee has timesheet record on the day of holiday
                    //if present, nPayRatex + 2
                    //if absent, nPayRatex must be 1
                    //if with OT, nPayRatex + 1
                    if (loRS.getString("xAbsentxx") != null){
                        //present
                        if (loRS.getString("xAbsentxx").equals("0")){
                            if (!(loRS.getDouble("xPayRatex") + 1 > 2.3)){
                                lsSQL = "UPDATE Employee_Timesheet SET" +
                                            " nPayRatex = nPayRatex + 1" +
                                        " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                            " AND dTransact = '2021-04-01'";
                                
                                if (oApp.executeQuery(lsSQL, "Employee_Timesheet", oApp.getBranchCode(), loRS.getString("sBranchCd")) <= 0){
                                    sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                                    oApp.rollbackTrans();
                                    return false;
                                }
                                
                                logwrapr.info("UPDATE Employee_Timesheet 2021-04-01" + "\t" + loRS.getString("sEmployID") + "\t" + loRS.getString("xPayRatex"));
                            }
                        }
                    }
                    //end - check if the employee has timesheet record on the day of holiday
                }
            }
        } catch (SQLException ex) {
            oApp.rollbackTrans();
            ex.printStackTrace();
            sMessage = ex.getMessage();
            return false;
        }
        
        oApp.commitTrans();
        return true;
    }
    
    public String getSQL_Master(){
        return "SELECT" +
                    " e.sBranchNm," +
                    " a.sEmployID," +
                    " b.sCompnyNm," +
                    " c.dTransact," +
                    " c.dAMInxxxx," +
                    " c.dAMOutxxx," +
                    " c.dPMInxxxx," +
                    " c.dPMOutxxx," +
                    " d.cAbsentxx," +
                    " d.cDeductxx," +
                    " d.nTardyxxx," +
                    " d.nUndrTime," +
                    " f.cAbsentxx xAbsentxx," +
                    " f.cDeductxx xDeductxx," +
                    " f.nPayRatex xPayRatex," + 
                    " a.sBranchCd" +
                " FROM Employee_Master001 a" + 
                    " LEFT JOIN Client_Master b" + 
                      " ON a.sEmployID = b.sClientID" + 
                    " LEFT JOIN Employee_Log c" + 
                      " ON a.sEmployID = c.sEmployID" + 
                      " AND c.dTransact = '2021-03-31'" + 
                    " LEFT JOIN Employee_Timesheet d" + 
                      " ON a.sEmployID = d.sEmployID" + 
                      " AND d.dTransact = '2021-03-31'" + 
                    " LEFT JOIN Branch e" + 
                      " ON a.sBranchCd = e.sBranchCd" + 
                    " LEFT JOIN Employee_Timesheet f" + 
                      " ON a.sEmployID = f.sEmployID" + 
                      " AND f.dTransact = '2021-04-01'" + 
                " WHERE a.cRecdStat = '1'" + 
                    " AND (a.sBranchCd LIKE 'N%' OR a.sBranchCd LIKE 'H%' OR a.sBranchCd LIKE 'A%')";
    }
    
    public String getMessage() {
        return sMessage;
    }
}