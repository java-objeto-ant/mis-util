/**
 * Michael Torres Cuison
 * 
 * Mac 2020-11-16
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

public class TLM_Fix implements UtilityValidator{
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
        String lsSQL = "SELECT" + 
                            "  a.sTransnox" +
                            ", a.dTransact" +
                            ", a.dFollowUp" +
                            ", a.dTargetxx" +
                            ", a.sInquiryx" +
                            ", d.sBranchNm" +
                            ", b.sCompnyNm" +
                            ", b.sClientID" +
                            ", c.sMobileNo" +
                            ", c.cSubscrbr" +
                        " FROM MC_Product_Inquiry a" +
                                " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID" +
                                " LEFT JOIN Client_Mobile c ON b.sClientID = c.sClientID AND b.sMobileNo = c.sMobileNo" +
                                " LEFT JOIN Branch d ON LEFT(a.sTransnox, 4) = d.sBranchCd" +
                        " WHERE dTransact >= '2023-01-01'" +
                            " AND cTranStat = '0'" +
                        " HAVING IFNULL(c.sMobileNo, '') <> ''" + 
                            " AND c.cSubscrbr IN ('')" +
                        " ORDER BY dTransact DESC";

        ResultSet loRS = instance.executeQuery(lsSQL);
        
        System.out.println("ITEM COUNT: " + MiscUtil.RecordCount(loRS));
        
        try {
            String lcSubscrbr;
            
            while (loRS.next()){
                lcSubscrbr = CommonUtils.classifyNetwork(loRS.getString("sMobileNo"));
                
                if (!lcSubscrbr.isEmpty()){
                    instance.beginTrans();
                    
                    lsSQL= "UPDATE Client_Mobile SET" +
                                "  cSubscrbr = " + SQLUtil.toSQL(lcSubscrbr) +
                                ", cRecdStat = '1'" +
                            " WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                                " AND sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo"));
                    
                    if (instance.executeQuery(lsSQL, "Client_Mobile", instance.getBranchCode(), "") <= 0){
                        if (!instance.getErrMsg().isEmpty()){
                            instance.rollbackTrans();
                            System.err.println(instance.getErrMsg());
                            System.exit(1);
                        }
                    }
                    
                    instance.commitTrans();
                }
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
