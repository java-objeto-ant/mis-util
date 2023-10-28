/**
 * Michael Torres Cuison
 * 
 * Mac 2022-02-27
 *      Started creating this object.
 */

package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.mis.util.others.ClassifyClientMobile;

public class GConnect implements UtilityValidator{
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
        String lsSQL = " SELECT *" + 
                        " FROM App_User_Activation_Request" +
                        " WHERE dTransact <= DATE_SUB(NOW(), INTERVAL 24 HOUR)" +
                            " AND cRqstType = '0'" +
                            " AND cTranStat = '0'";

        ResultSet loRS = instance.executeQuery(lsSQL);
        System.out.println("ITEM COUNT: " + MiscUtil.RecordCount(loRS));
        
        try {
            Date ldDate;
            
            while (loRS.next()){
                instance.beginTrans();
                
                ldDate = instance.getServerDate();
                
                lsSQL= "UPDATE App_User_Master SET" +
                            "  cInactive = '1'" +
                            ", dInactive = " + SQLUtil.toSQL(ldDate) +                            
                        " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx"));
                
                if (instance.executeQuery(lsSQL, "App_User_Master", instance.getBranchCode(), "") <= 0){
                    if (!instance.getErrMsg().isEmpty()){
                        instance.rollbackTrans();
                        System.err.println(instance.getErrMsg());
                        System.exit(1);
                    }
                }
                
                lsSQL= "UPDATE App_User_Activation_Request SET" +
                            "  cTranStat = '1'" +
                            ", sApproved = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(ldDate) +                            
                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                
                if (instance.executeQuery(lsSQL, "App_User_Activation_Request", instance.getBranchCode(), "") <= 0){
                    if (!instance.getErrMsg().isEmpty()){
                        instance.rollbackTrans();
                        System.err.println(instance.getErrMsg());
                        System.exit(1);
                    }
                }
                
                instance.commitTrans();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("App user requesting for deactivation processed successfull.");
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
