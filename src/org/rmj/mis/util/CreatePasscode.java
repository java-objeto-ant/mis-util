package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.StringHelper;
import org.rmj.replication.utility.LogWrapper;

public class CreatePasscode {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("PassCode", "GSecure.log");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("gRider");
        
        if(!instance.getErrMsg().isEmpty()){
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT *" +
                        " FROM HotLine_Outgoing" +
                        " WHERE sSourceCd = 'PASS'" +
                            " AND dTransact = " + SQLUtil.toSQL(SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)) +
                        "  LIMIT 1";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0) System.exit(0);
        
        String lsCode = StringHelper.prepad(String.valueOf(MiscUtil.getRandom(1, 9999)), 4, '0') ;
        System.out.println(lsCode);
        
        lsSQL = "SELECT"+
                    "  a.sEmployID"+
                    ", a.sMobileNo"+
                    ", IFNULL(CONCAT(LEFT(b.sFrstName, 1), LEFT(b.sMiddName, 1), LEFT(b.sLastName, 1)), 'GUA') xInitialx"+
                " FROM Employee_Text_Alert a"+
                    " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID"+
                " WHERE a.cStopAlrt = 0"+
                    " AND IF(a.dDateFrom = '0000-00-00', '1=1', (a.dDateFrom >= " + SQLUtil.toSQL(instance.getServerDate()) + " OR a.dDateThru <= " + SQLUtil.toSQL(instance.getServerDate()) + "))";
        System.out.println(lsSQL);
        
        loRS = instance.executeQuery(lsSQL);
        
        try {
            String lsTransNox;
            int lnCtr = 1;
            
            instance.beginTrans();
            
            while (loRS.next()){
                lsTransNox = MiscUtil.getNextCode("HotLine_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode());
                
                lsSQL = loRS.getString("xInitialx") + "-" + String.valueOf(lnCtr) + SQLUtil.dateFormat(instance.getServerDate(), "dd") +
                        " Guanzon Group\n" + "G*-" + lsCode + " is your " + SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_MEDIUM_DATE) +
                        " PASS CODE. Thank you and be safe. Ref#: " + lsTransNox;
                
                lsSQL = "INSERT INTO HotLine_Outgoing SET" +
                        "  sTransNox = " + SQLUtil.toSQL(lsTransNox) +
                        ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) +
                        ", sDivision = " + SQLUtil.toSQL("MIS") +
                        ", sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                        ", sMessagex = " + SQLUtil.toSQL(lsSQL) +
                        ", cSubscrbr = " + SQLUtil.toSQL(CommonUtils.classifyNetwork(loRS.getString("sMobileNo"))) +
                        ", dDueUntil = " + SQLUtil.toSQL(instance.getServerDate()) +
                        ", cSendStat = '0'" +
                        ", nNoRetryx = 0" +
                        ", nPriority = 1" +
                        ", sUDHeader = ''" +
                        ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                        ", sSourceCd = 'PASS'" +
                        ", cTranStat = '0'" +
                        ", sModified = ''" + 
                        ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                
                if (instance.executeQuery(lsSQL, "HotLine_Outgoing", instance.getBranchCode(), "") <= 0){
                    instance.rollbackTrans();
                    logwrapr.severe(instance.getErrMsg() + ";" + instance.getMessage());
                    System.exit(1);
                }
                
                lnCtr++;
            }
            
            instance.commitTrans();
        } catch (SQLException e) {
            instance.rollbackTrans();
            logwrapr.severe(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
    }
}
