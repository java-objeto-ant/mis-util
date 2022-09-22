package org.rmj.mis.util.lr;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.lib.net.MiscReplUtil;

public class GenerateLoanResigned {
    public static void main (String [] args){
        GRiderX instance = new GRiderX("gRider");
        
        if(!instance.getErrMsg().isEmpty()){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = getSQ_Master();
        
        ResultSet loRSMaster = instance.executeQuery(lsSQL);
        ResultSet loRSDetail;
        
        try {
            while (loRSMaster.next()){
                lsSQL = getSQ_Loans(loRSMaster.getString("sEmployID"));
                loRSDetail = instance.executeQuery(lsSQL);
                
                while (loRSDetail.next()){
                    lsSQL = loRSMaster.getString("sBranchNm") + "\t";
                    lsSQL += loRSMaster.getString("sCompnyNm") + "\t";
                    lsSQL += loRSMaster.getString("sEmployID") + "\t";
                    lsSQL += loRSMaster.getString("dFiredxxx") + "\t";
                    
                    lsSQL += loRSDetail.getString("sTransNox") + "\t";
                    lsSQL += loRSDetail.getString("sLoanName") + "\t";
                    lsSQL += loRSDetail.getString("dLoanDate") + "\t";
                    lsSQL += loRSDetail.getString("dFirstPay") + "\t";
                    lsSQL += loRSDetail.getString("nLoanAmtx") + "\t";
                    lsSQL += loRSDetail.getString("nMonAmort") + "\t";
                    lsSQL += loRSDetail.getString("nBalancex") + "\t";
                    lsSQL += loRSDetail.getString("cHoldDedx") + "\t";
                    lsSQL += loRSDetail.getString("cIntegSys") + "\t";
                    lsSQL += loRSDetail.getString("nPaymTerm") + "\n";
                    
                    MiscReplUtil.fileWrite("loans.txt", lsSQL, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static String getSQ_Master(){
        return "SELECT" +
                    "  a.sEmployID" +
                    ", c.sBranchNm" +
                    ", b.sCompnyNm" +
                    ", IFNULL(a.dFiredxxx, '') dFiredxxx" +
                " FROM Employee_Master001 a" +
                    " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID" +
                    " LEFT JOIN Branch c ON a.sPyBranch = c.sBranchCd" +
                " WHERE (a.sPyBranch LIKE 'M%' OR a.sPyBranch LIKE 'C%')" +
                    " AND a.cRecdStat = '0'" +
                " ORDER BY a.dFiredxxx";
    }
    
    public static String getSQ_Loans(String fsEmployID){
        return "SELECT" + 
                    "  sTransNox" +
                    ", sLoanName" +
                    ", dLoanDate" +
                    ", dFirstPay" +
                    ", nLoanAmtx" +
                    ", nMonAmort" +
                    ", nBalancex" +
                    ", cHoldDedx" +
                    ", cIntegSys" +
                    ", nPaymTerm" + 
                " FROM" +
                    " (SELECT" + 
                            "   a.sTransNox" +
                            " , b.sLoanName" +
                            " , a.dLoanDate" +
                            " , a.dFirstPay" +
                            " , a.nLoanAmtx" +
                            " , a.nAmortAmt nMonAmort" +
                            " , a.nBalancex" +
                            " , a.cHoldDedx" +
                            " , b.cIntegSys" +
                            " , nPaymTerm" + 
                        " FROM Employee_Loan_Master a" + 
                            " LEFT JOIN Loans b ON a.sLoanIDxx = b.sLoanIDxx" + 
                        " WHERE a.sEmployID = " + SQLUtil.toSQL(fsEmployID) + 
                            " AND a.nBalancex > 0" + 
                            " AND b.cIntegSys = '0'" + 
                            " AND a.cTranStat IN ('1', '2')" + 
                        " UNION" +
                        " SELECT" + 
                            "  sAcctNmbr sTransNox" +
                            ", CASE cLoanType" + 
                            "   WHEN '0'  THEN 'MC Sales'" + 
                            "   WHEN '1'  THEN 'Sidecar'" + 
                            "   WHEN '2'  THEN 'Power Products'" + 
                            "   ELSE 'Others'" + 
                            "  END sLoanName" +
                            ", dPurchase dLoanDate" +
                            ", dFirstPay dFirstPay" +
                            ", nPNValuex nLoanAmtx" +
                            ", nMonAmort" +
                            ", nABalance nBalancex" +
                            ", cAcctStat cHoldDedx" +
                            ", '1' cIntegSys" +
                            ", nAcctTerm nPaymTerm" + 
                        " FROM MC_AR_Master" + 
                        " WHERE sClientID = " + SQLUtil.toSQL(fsEmployID) + 
                            " AND nPNValuex > 0" + 
                            " AND nABalance > 0" + 
                            " AND cAcctStat IN ('0', '1')" + 
                            " AND cLoanType IN ('0', '1', '2', '3', '4')) x";
    
    }
}