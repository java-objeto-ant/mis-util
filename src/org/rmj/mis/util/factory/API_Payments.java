/**
 * Michael Torres Cuison
 * 
 * Mac 2020-11-16
 *      Started creating this object.
 */

package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class API_Payments implements UtilityValidator{
    private GRiderX instance;
    private String sMessage;
    
    @Override
    public void setGRider(GRiderX foValue) {
        instance = foValue;
        
        if (instance == null){
            System.err.println("Application driver is not set.");
            System.exit(1);
        }
    }

    @Override
    public boolean Run() {
        String lsSQL = "SELECT sTransNox, sAcctNmbr, sPayloadx FROM XAPITrans WHERE ISNULL(sAcctNmbr)";
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            if (MiscUtil.RecordCount(loRS) > 0){
                JSONObject loJSON;
                JSONParser loParser = new JSONParser();
                
                instance.beginTrans();
                while (loRS.next()){
                    loJSON = (JSONObject) loParser.parse(loRS.getString("sPayloadx"));
                    
                    System.out.println(loJSON.get("account") + "->>" + loRS.getString("sTransNox"));
                    
                    lsSQL = "UPDATE XAPITrans SET" +
                                "  sAcctNmbr = " + SQLUtil.toSQL(loJSON.get("account")) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    
                    if (instance.executeQuery(lsSQL, "XAPITrans", instance.getBranchCode(), "") <= 0){
                        instance.rollbackTrans();
                        System.err.println(instance.getMessage());
                        System.err.println(instance.getErrMsg());
                        System.exit(1);
                    }
                }
                instance.commitTrans();
            }
        } catch (SQLException | ParseException ex) {
            instance.rollbackTrans();
            ex.printStackTrace();
            setMessage(ex.getMessage());
            
            return false;
        }
        
        System.out.println("Utility processing done.");        

        return true;
    }

    @Override
    public void setMessage(String fsValue) {
        sMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return sMessage;
    }
}
