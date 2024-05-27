package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class CreateGuanzonCodeMP {
    public static void main (String [] args){
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
            System.exit(1);
        }
        
        String lsSQL = "SELECT * FROM CP_Model_GSCM WHERE sGnznCode IS NULL";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) <= 0) System.exit(1);
        
        try {
            instance.beginTrans();
            
            while (loRS.next()){
                lsSQL = "UPDATE CP_Model_GSCM SET" +
                            "  sGnznCode = " + SQLUtil.toSQL(MiscUtil.getNextCode("CP_Model_GSCM", "sGnznCode", true, instance.getConnection(), instance.getBranchCode())) +
                        " WHERE sModelIDx = " + SQLUtil.toSQL(loRS.getString("sModelIDx")) +
                            " AND sColorIDx = " + SQLUtil.toSQL(loRS.getString("sColorIDx"));
                
                if (instance.executeQuery(lsSQL, "CP_Model_GSCM", instance.getBranchCode(), "") != 1){
                    instance.rollbackTrans();
                    System.err.println("Unable to execute update.");
                    System.exit(1);
                }
            }
            
            instance.commitTrans();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Thank you!");
        System.exit(0);
    }
}
