package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class ExtactAPITrans {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        final String PRODUCTID = "IntegSys";
        final String USERID = "M001111122";
        
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
        
        String lsSQL = "SELECT * FROM XAPITrans WHERE dReceived BETWEEN '2020-12-01 00:00:00' AND '2020-12-08 23:59:30'"; 
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON;
        JSONParser loParser = new JSONParser();
        
        try {
            while(loRS.next()){
                //System.out.println(loRS.getString("dReceived") + "\t" + loRS.getString("sReferNox") + "\t" + loRS.getString("sAcctNmbr") + "\t");
                
                loJSON = (JSONObject) loParser.parse(loRS.getString("sPayloadx"));
                
                System.out.println((String) loJSON.get("branch") + "\t" + 
                                    (String) loJSON.get("referno") + "\t" + 
                                    (String) loJSON.get("datetime") + "\t" + 
                                    (String) loJSON.get("account") + "\t" + 
                                    (String) loJSON.get("name") + "\t" + 
                                    (String) loJSON.get("address") + "\t" + (String) loJSON.get("mobile") + "\t" + (String) loJSON.get("amount") + "\t");
            }
        } catch (SQLException | ParseException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
        
    }
}
