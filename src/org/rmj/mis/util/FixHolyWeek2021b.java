package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class FixHolyWeek2021b {
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
        
        GRiderX instance = new GRiderX("TeleMktg");
        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        FixHolidayb fixHoliday = new FixHolidayb(instance, logwrapr);
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

class FixHolidayb{
    private GRiderX oApp;
    private String sMessage;
    
    private LogWrapper logwrapr;
    
    public FixHolidayb(GRiderX foApp, LogWrapper logwrapr){
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
                if (loRS.getString("cAbsentxx") == null){
                    lsSQL = "SELECT *";
                    
                    lsSQL = "INSERT INTO Employee_Timesheet SET" +
                                "  sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                ", dTransact = '2021-03-31'" +
                                ", cAbsentxx = '1'" +
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
                                "  cAbsentxx = '1'" +
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
                    
                //check if the employee has timesheet record on the day of holiday
                //if present, nPayRatex + 2
                //if absent, nPayRatex must be 1
                //if with OT, nPayRatex + 1
                if (loRS.getString("xAbsentxx") != null){
                    //present
                    if (loRS.getString("xAbsentxx").equals("0")){
                        if (loRS.getDouble("xPayRatex") == 1){
                            lsSQL = "UPDATE Employee_Timesheet SET" +
                                        " nPayRatex = 2.00" +
                                    " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                        " AND dTransact = '2021-04-01'";
                        } else if (loRS.getDouble("xPayRatex") >= 2){
                            lsSQL = "";    
                        } else {
                            lsSQL = "UPDATE Employee_Timesheet SET" +
                                        " nPayRatex = nPayRatex + 1" +
                                    " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                        " AND dTransact = '2021-04-01'";
                        }
                        
                        if (!lsSQL.equals("")){
                            if (oApp.executeQuery(lsSQL, "Employee_Timesheet", oApp.getBranchCode(), loRS.getString("sBranchCd")) <= 0){
                                sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                                oApp.rollbackTrans();
                                return false;
                            }

                            logwrapr.info(lsSQL);
                        }
                    } else {
                        lsSQL = "UPDATE Employee_Timesheet SET" +
                                    " cDeductxx = '0'" +
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2021-04-01'";

                        if (oApp.executeQuery(lsSQL, "Employee_Timesheet", oApp.getBranchCode(), loRS.getString("sBranchCd")) <= 0){
                            sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                            oApp.rollbackTrans();
                            return false;
                        }

                        logwrapr.info(lsSQL);
                    }
                    
                    lsSQL = "UPDATE Employee_Timesheet SET" +
                                " cDeductxx = '0'" +
                            " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                " AND dTransact = '2021-04-02'";
                    
                    if (oApp.executeQuery(lsSQL, "Employee_Timesheet", oApp.getBranchCode(), loRS.getString("sBranchCd")) <= 0){
                            sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                            oApp.rollbackTrans();
                            return false;
                        }

                    logwrapr.info(lsSQL);
                }
                //end - check if the employee has timesheet record on the day of holiday
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
                    " AND  a.sBranchCd IN ('C045', 'C031', 'C021', 'C028', 'C018', 'C088', 'C060', 'C053', 'C016', 'C037', 'C052', 'C090', 'C046', 'C061', 'C097', 'M073', 'M103', 'M177', 'M027', 'M138', 'M078', 'M012', 'M017', 'M030', 'M029', 'M026', 'M034', 'M115', 'M153', 'M040', 'M044', 'M039', 'M075', 'M054',  'M091', 'M121', 'M046', 'M081')";
    }
    
    public String getMessage() {
        return sMessage;
    }
}