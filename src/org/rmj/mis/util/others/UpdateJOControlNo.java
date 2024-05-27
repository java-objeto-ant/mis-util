package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class UpdateJOControlNo {
    public static void main(String [] args){
        final String PRODUCTID = "IntegSys";
        final String USERID = "M001111122";
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
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

        try {
            String lsSQL = "SELECT YEAR(CURRENT_TIMESTAMP)";
            //get the current year
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            loRS.next();
            System.out.println(loRS.getString(1));
            String lsBranch = poGRider.getBranchCode() +  "22";
//            String lsBranch = poGRider.getBranchCode() +  loRS.getString(1).substring(2);
            
            // create a scanner so we can read the command-line input
            Scanner scanner = new Scanner(System.in);
            
            //ask the user to input the control number with 12 character of the branch
            System.out.print("Please enter the 12 character control number: ");
            String lsCtrlNoxx = scanner.next().trim();
            
            //control number must not be empty
            if (lsCtrlNoxx.isEmpty()){
                System.err.println("Control number must not be empty.\nPlease be careful with what your entry.");
                System.exit(1);
            }
            //control number must be on the same branch
            if (!lsCtrlNoxx.substring(0, 4).equals(poGRider.getBranchCode())){
                System.err.println("Invalid control number for this branch.\nPlease be careful with what your entry.");
                System.exit(1);
            }
            //control number must be on the same branch and current year
            if (!lsCtrlNoxx.substring(0, 6).equals(lsBranch)){
                System.err.println("Control number is not for the current year.\nPlease be careful with what your entry.");
                System.exit(1);
            }
            
            lsBranch = lsBranch + "%";
            //check control number if exists on branch table
            lsSQL = "SELECT sTransNox, sCtrlNoxx" +
                        " FROM JobOrderBranch_Master" +
                        " WHERE sTransNox LIKE " + SQLUtil.toSQL(lsBranch) +                            
                            " AND sCtrlNoxx = " + SQLUtil.toSQL(lsCtrlNoxx) +
                        " ORDER BY sTransNox";
            loRS = poGRider.executeQuery(lsSQL);
            long lnCtr = MiscUtil.RecordCount(loRS);
             
            if (lnCtr == 0){
                System.err.println("Control number is not found on branch table.");
                System.exit(1);
            } else if (lnCtr == 1) {
                System.out.println("Branch JO control number is updated based on the given data.");
                System.exit(0);
            }
            //end - check control number if exists on branch table
            
            //update transaction with control number of 10 characters
            lsSQL = "SELECT sTransNox, sCtrlNoxx" +
                            " FROM JobOrderBranch_Master" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(lsBranch) +
                                " AND sCtrlNoxx <> ''" +
                                " AND sCtrlNoxx <> " + SQLUtil.toSQL(lsCtrlNoxx) +
                            " ORDER BY sTransNox";
            loRS = poGRider.executeQuery(lsSQL);
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
            //end - update transaction with control number of 10 characters
            
            //update transaction with control number of 12 characters
            lsSQL = "SELECT sTransNox, sCtrlNoxx" +
                            " FROM JobOrderBranch_Master" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(lsBranch) +
                                " AND sCtrlNoxx <> ''" +
                                " AND sCtrlNoxx = " + SQLUtil.toSQL(lsCtrlNoxx) +
                            " ORDER BY sTransNox";
            loRS = poGRider.executeQuery(lsSQL);
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
            //end - update transaction with control number of 12 characters
            
            System.out.println("Updated successully...");
            System.exit(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
