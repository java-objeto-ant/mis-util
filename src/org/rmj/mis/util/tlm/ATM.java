package org.rmj.mis.util.tlm;

import org.rmj.appdriver.GRider;

public class ATM {
    public static void main(String [] args){
        final String PRODUCTID = "TeleMktg";
        final String USERID = "M001111122";
        
        GRider poGRider = new GRider(PRODUCTID);

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        
        ATMMaker instance = new ATMMaker(poGRider);
        instance.CreatePasscode();
    }
}
