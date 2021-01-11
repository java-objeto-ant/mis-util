package org.rmj.mis.util.tlm;

import java.sql.ResultSet;
import java.util.List;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class ATMMaker {
    private GRider poGRider;
    private String psMessage;

    public ATMMaker(GRider foGRider){
        poGRider = foGRider;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public boolean CreatePasscode(){        
        //check if passcode messages was created for the day.
        ResultSet loRS = poGRider.executeQuery(getSQ_PassSMS());
        
        boolean bCreated = MiscUtil.RecordCount(loRS) > 0;
        
        String lsSQL;
        ResultSet loRSEmp;
        if (bCreated){
            //messages was created for the day.
            //now, check sms create for all employees registered
            lsSQL = "SELECT sEmployID FROM (" + getSQ_PassEmp() + ") x";
            List mEmployees = MiscUtil.rows2Map(poGRider.executeQuery(lsSQL));
            
            if (mEmployees.size() > 0){
            
            }
        } else {
            //messages was not created for the day.
        
        }
        
        
        
        return true;
    }
    
    private String getSQ_PassSMS(){
        return "SELECT *" +
                " FROM HotLine_Outgoing" + 
                " WHERE sSourceCd = 'PASS'" +
                    " AND dTransact = " + SQLUtil.toSQL(SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_SHORT_DATE));
    }
    
    private String getSQ_PassEmp(){
        return "SELECT" +
                    "  sEmployID" +
                    ", sMobileNo" +
                " FROM Employee_Text_Alert" +
                " WHERE cStopAlrt = 0" +
                    " AND IF(dDateFrom = '0000-00-00', '1=1', (dDateFrom >= " + SQLUtil.toSQL(SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)) +
                        " OR dDateThru <= " + SQLUtil.toSQL(SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)) + "))";
    }
    
    public boolean CreateA3T(){
        return true;
    }
}
