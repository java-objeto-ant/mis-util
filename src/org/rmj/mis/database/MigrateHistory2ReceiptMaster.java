package org.rmj.mis.database;

import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.lib.net.LogWrapper;

public class MigrateHistory2ReceiptMaster {
    public static void main(String [] args) {
        final String SOURCEX = "Receipt_Master_History";
        final String DESTNAT = "Receipt_Master";
        
        LogWrapper logwrapr = new LogWrapper("receipt_master", "db-migrate.log");
        logwrapr.info("Start of Process!");
        
        if (args.length != 1){
            logwrapr.warning("Invalid parameter detected.");
            logwrapr.info("End of Process!");
            System.exit(1);
        }
        
        if (!CommonUtils.isDate(args[0], SQLUtil.FORMAT_SHORT_DATE)){
            logwrapr.warning("Parameter is not a valid date.");
            logwrapr.info("End of Process!");
            System.exit(1);
        }
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("gRider");
        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL;
        
        instance.beginTrans();
        
        lsSQL = "INSERT INTO " + DESTNAT + 
                    " SELECT * FROM " + SOURCEX + 
                    " WHERE dTransact < " + SQLUtil.toSQL(args[0]);
        
        logwrapr.info(lsSQL);
        instance.executeQuery(lsSQL, "xxxTableAll", instance.getBranchCode(), "");
        
        lsSQL = "DELETE FROM " + SOURCEX + 
                " WHERE dTransact < " + SQLUtil.toSQL(args[0]);
        
        logwrapr.info(lsSQL);
        instance.executeQuery(lsSQL, "xxxTableAll", instance.getBranchCode(), "");

        instance.commitTrans();

        logwrapr.info("End of Process!");
        System.exit(0);
    }
}