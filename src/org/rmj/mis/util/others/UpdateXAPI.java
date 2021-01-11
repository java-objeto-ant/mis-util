package org.rmj.mis.util.others;

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

public class UpdateXAPI {
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
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
        
        String lsSQL = "SELECT sTransNox, sAcctNmbr, sPayloadx FROM XAPITrans WHERE ISNULL(sAcctNmbr)";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            if (MiscUtil.RecordCount(loRS) > 0){
                JSONObject loJSON;
                JSONParser loParser = new JSONParser();
                
                poGRider.beginTrans();
                while (loRS.next()){
                    loJSON = (JSONObject) loParser.parse(loRS.getString("sPayloadx"));
                    
                    System.out.println(loJSON.get("account") + "->>" + loRS.getString("sTransNox"));
                    
                    lsSQL = "UPDATE XAPITrans SET" +
                                "  sAcctNmbr = " + SQLUtil.toSQL(loJSON.get("account")) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    
                    if (poGRider.executeQuery(lsSQL, "XAPITrans", poGRider.getBranchCode(), "") <= 0){
                        poGRider.rollbackTrans();
                        System.err.println(poGRider.getMessage());
                        System.err.println(poGRider.getErrMsg());
                        System.exit(1);
                    }
                }
                poGRider.commitTrans();
            }
        } catch (SQLException | ParseException ex) {
            poGRider.rollbackTrans();
            Logger.getLogger(UpdateXAPI.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        System.out.println("Utility processing done.");
        System.exit(0);
    }
}
