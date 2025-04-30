package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class UpdatePOSDailySummary {
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
        final String USERID = "M001111122";
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider poGRider = new GRider(PRODUCTID);       

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            MiscUtil.close(poGRider.getConnection());
            System.exit(1);
        }
        
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            MiscUtil.close(poGRider.getConnection());
            System.exit(1);
        }
        
        String lsSQL = "SELECT * FROM Daily_Summary WHERE cTranStat = '2' ORDER BY sTranDate";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) <= 0) System.exit(0);
        
        poGRider.beginTrans();
        
        try {
            double lnAccumulated = 0;
            double lnDailySales = 0;
            
            while(loRS.next()){
                lnDailySales = loRS.getDouble("nSalesAmt") - loRS.getDouble("nDiscount");
                lnAccumulated += lnDailySales;
                
                lsSQL = "UPDATE Daily_Summary SET nAccuSale = " + lnAccumulated + " WHERE sTranDate = " + SQLUtil.toSQL(loRS.getString("sTranDate"));
                poGRider.executeQuery(lsSQL, "Daily_Summary", poGRider.getBranchCode(), "");
            }
        } catch (SQLException e) {
            poGRider.rollbackTrans();
            System.err.println(e.getMessage());
            System.exit(1);
        }
                
        poGRider.commitTrans();
        
        System.exit(0);
    }
}
