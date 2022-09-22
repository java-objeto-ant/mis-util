package org.rmj.mis.util.hcm;

import org.rmj.mis.util.factory.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class Batch_Shift_Movement implements UtilityValidator{
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
            //get all records that are effective tommorow
            String lsSQL = "SELECT sTransNox, sApproved" +
                            " FROM Employee_Shift_Batch_Change_Master" +
                            " WHERE cTranStat = '1'" +
                                " AND sBranchCd LIKE 'P%'" +
                                " AND dEffectve = " + SQLUtil.toSQL(SQLUtil.dateFormat(MiscUtil.dateAdd(instance.getServerDate(), 1), SQLUtil.FORMAT_SHORT_DATE));
            
            ResultSet loMaster = instance.executeQuery(lsSQL);
            ResultSet loDetail;
            int lnCtr;
            
            while (loMaster.next()){
                lsSQL = "SELECT * FROM Employee_Shift_Batch_Change_Detail" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loMaster.getString("sTransNox")) +
                        " ORDER BY nEntryNox";
                
                loDetail = instance.executeQuery(lsSQL);
                
                instance.beginTrans();
                
                while (loDetail.next()){
                    for (lnCtr = 1; lnCtr <= 7; lnCtr++){
                        lsSQL = loDetail.getString("sShftDay" + lnCtr);
                        if (lsSQL != null){
                            if (lsSQL.equalsIgnoreCase("xxx") || lsSQL.equalsIgnoreCase("yyy")){
                                lsSQL = "INSERT INTO Employee_Rest_Day SET" +
                                        "  sEmployID = " + SQLUtil.toSQL(loDetail.getString("sEmployID")) +
                                        ", nRestDayx = " + lnCtr + 
                                        ", cSpecialx = " + SQLUtil.toSQL(lsSQL.equalsIgnoreCase("xxx") ? "0" : "1") +
                                        ", sModified = " + SQLUtil.toSQL(loMaster.getString("sApproved")) +
                                        ", dModified = " + SQLUtil.toSQL(instance.getServerDate()) +
                                        " ON DUPLICATE KEY UPDATE" +
                                        "  cSpecialx = " + SQLUtil.toSQL(lsSQL.equalsIgnoreCase("xxx") ? "0" : "1") +
                                        ", sModified = " + SQLUtil.toSQL(loMaster.getString("sApproved")) +
                                        ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                                
                                instance.executeQuery(lsSQL, "Employee_Rest_Day", instance.getBranchCode(), "");
                            } else {
                                lsSQL = "INSERT INTO Employee_Shift SET" +
                                        "  sEmployID = " + SQLUtil.toSQL(loDetail.getString("sEmployID")) +
                                        ", nDayOfWik = " + lnCtr +
                                        ", sShiftIDx = " + SQLUtil.toSQL(lsSQL) +
                                        " ON DUPLICATE KEY UPDATE" +
                                        "  sShiftIDx = " + SQLUtil.toSQL(lsSQL);
                                
                                instance.executeQuery(lsSQL, "Employee_Shift", instance.getBranchCode(), "");
                            }
                            
                            if (!instance.getErrMsg().isEmpty()){
                                instance.rollbackTrans();
                                setMessage(instance.getErrMsg());
                                return false;
                            }
                        }
                    }
                }
                
                lsSQL = "UPDATE Employee_Shift_Batch_Change_Master SET" +
                        " cTranStat = '2'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loMaster.getString("sTransNox"));
                    
                if (instance.executeQuery(lsSQL, "Employee_Shift_Batch_Change_Master", instance.getBranchCode(), "") <= 0){
                    instance.rollbackTrans();
                    setMessage(instance.getErrMsg() + "\n" + instance.getMessage());
                    return false;
                }
                
                instance.commitTrans();
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
}
