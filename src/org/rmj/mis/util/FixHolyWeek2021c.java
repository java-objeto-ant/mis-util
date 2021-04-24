package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class FixHolyWeek2021c {
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
        
        FixHolidayc fixHoliday = new FixHolidayc(instance, logwrapr);
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

class FixHolidayc{
    private GRiderX oApp;
    private String sMessage;
    
    private LogWrapper logwrapr;
    
    public FixHolidayc(GRiderX foApp, LogWrapper logwrapr){
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
                if (loRS.getString("cAbsentxx").equals("0")){
                    lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cAbsentxx = '0'" +
                                    ", cDeductxx = '0'" +
                                    ", nPayRatex = 2" + 
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2021-04-09'";
                } else {
                    //check if the employee has attendance on the holiday
                    if (loRS.getString("dAMInxxxx") != null || loRS.getString("dAMOutxxx") != null ||
                            loRS.getString("dPMInxxxx") != null || loRS.getString("dPMOutxxx") != null){
                        lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cAbsentxx = '0'" +
                                    ", cDeductxx = '0'" +
                                    ", nPayRatex = 2" + 
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2021-04-09'";
                    } else{
                        lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cDeductxx = '0'" +
                                    ", nPayRatex = 1" + 
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2021-04-09'";
                    }
                }

                if (oApp.executeQuery(lsSQL, "Employee_Timesheet", oApp.getBranchCode(), loRS.getString("sBranchCd")) <= 0){
                    sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                    oApp.rollbackTrans();
                    return false;
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
                      " AND c.dTransact = '2021-04-09'" + 
                    " LEFT JOIN Employee_Timesheet d" + 
                      " ON a.sEmployID = d.sEmployID" + 
                      " AND d.dTransact = '2021-04-09'" + 
                    " LEFT JOIN Branch e" + 
                      " ON a.sBranchCd = e.sBranchCd" + 
                    " LEFT JOIN Employee_Timesheet f" + 
                      " ON a.sEmployID = f.sEmployID" + 
                      " AND f.dTransact = '2021-04-09'" + 
                " WHERE a.cRecdStat = '1'" + 
                    " AND  a.sBranchCd IN ('C045', 'C031', 'C021', 'C028', 'C018', 'C088', 'C060', 'C053', 'C016', 'C037', 'C052', 'C090', 'C046', 'C061', 'C097', 'M073', 'M103', 'M177', 'M027', 'M138', 'M078', 'M012', 'M017', 'M030', 'M029', 'M026', 'M034', 'M115', 'M153', 'M040', 'M044', 'M039', 'M075', 'M054',  'M091', 'M121', 'M046', 'M081')";
    }
    
    public String getMessage() {
        return sMessage;
    }
    
    //computed date applied from and to of leave on ncr+ if greater than nwithpay, set cleave of employee timesheet 04.01.2021 to 0 else 1
}