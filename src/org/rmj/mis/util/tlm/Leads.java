package org.rmj.mis.util.tlm;

import java.io.IOException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;

public class Leads {
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
        
        /*try {
            Process process = new ProcessBuilder("C:\\GGC_Systems\\vb.net\\CreateSource.exe").start();
            System.out.println("Create source utility initialized...");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.err.println("RETURN CODE: 1" );
            System.exit(1);
        }*/

        boolean lbSuccess;
        //Credit Appliation to Leads
        System.out.println("Initializing Credit Application to Leads...");
        CI2TLM instance = new CI2TLM(poGRider);
        //Updating leads that already purchased.
        System.out.println("Updating leads that already purchased....");
        instance.UpdateReleased();
        if (args.length > 0){
            if (args[0].equals("1"))
                lbSuccess = instance.Create(true);
            else 
                lbSuccess = instance.Create(false);
        } else lbSuccess = instance.Create(true);
        
        //MC Sales to Leads
        System.out.println("Initializing MC Sales to Leads...");
        MCSO2TLM mcso = new MCSO2TLM(poGRider);
        lbSuccess = mcso.Create();
        
        //logout user and close connection.
        poGRider.logoutUser();
        MiscUtil.close(poGRider.getConnection()); 
        
        //return exit code
        if (lbSuccess){
            System.out.println("RETURN CODE: 0" );
            System.exit(0);
        } else{
            System.err.println("RETURN CODE: 1" );
            System.exit(1);
        }   
    }
}
