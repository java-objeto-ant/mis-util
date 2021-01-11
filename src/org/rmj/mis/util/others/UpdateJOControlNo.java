package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class UpdateJOControlNo {
    public static void main(String [] args){
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
        
        //GMC Manaoag last correct control no: M059191855
        //GMC Manaoag repeated control no: M05919001856
        //GMC Manaoag last fixed control no: M05919185630
        
        //UEMI Cainta last correct control no: M017199999
        //UEMI Cainta repeated control no: M01719010000
        //UEMI Cainta last fixed control no: 
        String lsSQL = "SELECT sTransNox, sCtrlNoxx" +
                        " FROM JobOrderBranch_Master" +
                        " WHERE sTransNox LIKE 'M11020%'" + 
                            " AND sCtrlNoxx <> ''" +
                            " AND sCtrlNoxx = 'M11020000392'" +
                        " ORDER BY sTransNox";
        
                        
        
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            poGRider.beginTrans();
            while (loRS.next()){
                if (loRS.getString("sCtrlNoxx").length() == 10){
                    lsSQL = "UPDATE JobOrderBranch_Master SET" +
                                "  sCtrlNoxx = " + SQLUtil.toSQL(loRS.getString("sCtrlNoxx").substring(0, 6) + "00" + loRS.getString("sCtrlNoxx").substring(6)) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    System.out.println(lsSQL);
                    
                    if (poGRider.executeQuery(lsSQL, "JobOrderBranch_Master", poGRider.getBranchCode(), poGRider.getBranchCode()) == 0){
                        System.err.println(poGRider.getErrMsg() + "; " + poGRider.getMessage());
                        poGRider.rollbackTrans();
                        break;
                    }
                } else{
                    lsSQL = "UPDATE JobOrderBranch_Master SET" +
                            "  sCtrlNoxx = " + SQLUtil.toSQL(MiscUtil.getNextCode("JobOrderBranch_Master", "sCtrlNoxx", true, poGRider.getConnection(), poGRider.getBranchCode())) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));

                    if (poGRider.executeQuery(lsSQL, "JobOrderBranch_Master", poGRider.getBranchCode(), poGRider.getBranchCode()) == 0){
                        System.err.println(poGRider.getErrMsg() + "; " + poGRider.getMessage());
                        poGRider.rollbackTrans();
                        break;
                    }
                }
            }
            poGRider.commitTrans();
            System.out.println("Updated successully...");
        } catch (SQLException ex) {
            Logger.getLogger(UpdateJOControlNo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
