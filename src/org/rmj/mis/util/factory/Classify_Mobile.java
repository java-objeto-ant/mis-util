/**
 * Michael Torres Cuison
 * 
 * Mac 2022-02-27
 *      Started creating this object.
 */

package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.mis.util.others.ClassifyClientMobile;

public class Classify_Mobile implements UtilityValidator{
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
        String lsSQL = "SELECT a.sClientID, a.sMobileNo, b.sMobileNo xMobileNo, b.cSubscrbr, b.cConfirmd, b.dConfirmd" +
                        " FROM Client_Master a" +
                            ", Client_Mobile b" +
                        " WHERE a.sClientID = b.sClientID" +
                            " AND a.sMobileNo = b.sMobileNo" +
                            " AND LENGTH(TRIM(REPLACE(REPLACE(REPLACE(b.sMobileNo, '(', ''), ')', ''), '-', ''))) = 11" +
                        " HAVING b.cSubscrbr IS NULL" +
                        " LIMIT 10000";

        ResultSet loRS = instance.executeQuery(lsSQL);
        System.out.println("ITEM COUNT: " + MiscUtil.RecordCount(loRS));
        
        try {
            String lcSubscrbr;
            
            while (loRS.next()){
                lcSubscrbr = CommonUtils.classifyNetwork(loRS.getString("sMobileNo"));
                
                lsSQL= "UPDATE Client_Mobile SET" +
                            "  cSubscrbr = " + SQLUtil.toSQL(lcSubscrbr) +
                            ", cRecdStat = '1'" +
                            ", cConfirmd = " + SQLUtil.toSQL(lcSubscrbr.isEmpty() ? "X" : "1") +
                            ", dConfirmd = " + SQLUtil.toSQL(instance.getServerDate()) +
                        " WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                            " AND sMobileNo = " + SQLUtil.toSQL(loRS.getString("xMobileNo"));
                
                instance.beginTrans();
                if (instance.executeQuery(lsSQL, "Client_Mobile", instance.getBranchCode(), "") <= 0){
                    if (!instance.getErrMsg().isEmpty()){
                        instance.rollbackTrans();
                        System.err.println(instance.getErrMsg());
                        System.exit(1);
                    }
                }
                instance.commitTrans();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClassifyClientMobile.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        System.out.println("Client Mobile classified successfuly.");
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
