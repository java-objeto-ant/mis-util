package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.StringHelper;
import org.rmj.replication.utility.LogWrapper;

public class ValidateMPPromo {
    public static void main(String [] args){
        LogWrapper logwrapr = new LogWrapper("Promo", "MobilePhone.log");
        
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
                        " FROM CP_Model_Price" +
                        " WHERE dPriceTru < " + SQLUtil.toSQL(SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)) +
                        " AND sMPCatIDx <> ''" +
                        " ORDER BY dPricexxx";
        
        ResultSet loRS = instance.executeQuery(lsSQL);

        loRS = instance.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){

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
