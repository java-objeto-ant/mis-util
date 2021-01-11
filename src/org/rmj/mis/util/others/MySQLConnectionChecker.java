package org.rmj.mis.util.others;

import org.json.simple.JSONObject;
import org.rmj.appdriver.agentfx.thread.MySQLConnectionThread;
import org.rmj.appdriver.agentfx.listener.OnEventListener;
import org.rmj.appdriver.agentfx.CommonUtils;

public class MySQLConnectionChecker {   
    static final int INTERVAL = 180000;
    
    public static void main (String [] args){
        //"ATM ALL NET A*|Biometrics*"
        if (args.length <= 0){
            System.err.println("ERROR MESSAGE: Invalid Paramter Detected!!!");
            System.exit(1);
        }
        
        String [] laApp = args[0].split("|");
        
        OnEventListener cbMySQLConnect = new OnEventListener() {
            @Override
            public void onSuccess(JSONObject success) {
                System.out.println((String) success.get("message"));
                System.exit(0);
            }

            @Override
            public void onFailure(JSONObject failure) {
                System.err.println("ERROR MESSAGE: " + (String) failure.get("message"));
                
                for (int lnCtr = 0; lnCtr <= laApp.length -1; lnCtr ++){
                    CommonUtils.killProcess(laApp[lnCtr]);
                }                
                System.exit(1);
            }
        };
    
        MySQLConnectionThread tMySQL = new MySQLConnectionThread("gRider", cbMySQLConnect, INTERVAL);
        tMySQL.start();
    }    
}
