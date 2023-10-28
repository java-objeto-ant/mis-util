package org.rmj.mis.util.others;

import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;

public class UpdateAccountChart {
    public static void main(String [] args){
        final String PRODUCTID = "IntegSys";
        final String USERID = "M001111122";
        final String TABLENAME = "Account_Chart";
        
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
        
        
        
    }
}
