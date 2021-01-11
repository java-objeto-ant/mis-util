package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.constants.TransactionStatus;
import org.rmj.appdriver.constants.UserRight;
import org.rmj.replication.utility.LogWrapper;

public class UpdateSPTransfer {
    public static void main(String [] args){
        StringBuilder lsSQL = new StringBuilder();
      
        //kalyptus - 2017.10.10 09:17am
        //load MC_Product_Inquiry: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        //                 and if Walk inquiry after 7 days ago... Previously it was 60 days ago.
        lsSQL.append(" SELECT '' sMobileNo, sClientID, dFollowUp, sTransNox, 'MC_Product_Inquiry' sTableNme, dTransact"
                   + " FROM MC_Product_Inquiry"
                   + " WHERE cTranStat = '0'"
                     + " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01' OR "
                          + " dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY) AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))"
                     + " AND (sInquiryx <> 'BI' OR (sInquiryx = 'BI' AND DATE_ADD(dTransact, INTERVAL 8 DAY) < CURRENT_DATE()))" 
                     + " AND IFNULL(dTargetxx, '1900-01-01') <= DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE)");

        //mac - 2019.06.28 09:17am
        //load MP_Product_Inquiry: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        //                 and if Walk inquiry after 7 days ago... Previously it was 60 days ago.
        lsSQL.append(" UNION ");
        lsSQL.append(" SELECT '' sMobileNo, sClientID, dFollowUp, sTransNox, 'MP_Product_Inquiry' sTableNme, dTransact"
                   + " FROM MP_Product_Inquiry"
                   + " WHERE cTranStat = '0'"
                     + " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01' OR "
                          + " dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY) AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))"
                     + " AND (sInquiryx <> 'BI' OR (sInquiryx = 'BI' AND DATE_ADD(dTransact, INTERVAL 8 DAY) < CURRENT_DATE()))" 
                     + " AND IFNULL(dTargetxx, '1900-01-01') <= DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE)");

        //load MC_Referral: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        lsSQL.append(" UNION ");
        lsSQL.append(" SELECT '' sMobileNo, sClientID, dFollowUp, sTransNox, 'MC_Referral' sTableNme, dTransact"
                   + " FROM MC_Referral"
                   + " WHERE cTranStat = '0'"
                     + " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01' OR "
                          + " dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY) AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))");

        //load Call_Incoming: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        lsSQL.append(" UNION ");
        lsSQL.append(" SELECT sMobileNo, '' sClientID, dFollowUp, sTransNox, 'Call_Incoming' sTableNme, dTransact"
                   + " FROM Call_Incoming"
                   + " WHERE cTranStat = '0'"
                     + " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01' OR "
                          + " dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY) AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))"
                     + " AND sMobileNo <> ''");

        //load SMS_Incoming: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        lsSQL.append(" UNION ");
        lsSQL.append(" SELECT sMobileNo, '' sClientID, dFollowUp, sTransNox, 'SMS_Incoming' sTableNme, dTransact"
                   + " FROM SMS_Incoming"
                   + " WHERE cTranStat = '0'"
                     + " AND cReadxxxx = '1'"
                     + " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01' OR "
                          + " dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY) AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))" 
                     + " AND sMessagex NOT LIKE '%FSE%'");

        //load SMS_Incoming: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        //kalyptus - 2018.04.18 10:59am
        //contact MP costumer only...
        lsSQL.append(" UNION ");
        lsSQL.append(" SELECT '' sMobileNo, sClientID, dFollowUp, sClientID sTransNox, 'TLM_Client' sTableNme, dFollowUp dTransact"
                   + " FROM TLM_Client"
                   + " WHERE cTranStat = '0'"
                     + " AND cSourceCD = 'MP'" 
                     + " AND (IFNULL(dFollowUp, '1900-01-01') = '1900-01-01' OR "
                          + " dFollowUp BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY) AND DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE))");

        lsSQL.append(" ORDER BY dFollowUp DESC, dTransact DESC");
        
        System.out.println(lsSQL.toString());
        
        System.exit(0);
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
        
        CheckSPTransfer instance = new CheckSPTransfer(poGRider);
        if (instance.CancelTransactions())
            System.out.println(instance.getMessage());
        else
            System.err.println(instance.getMessage());
        
        MiscUtil.close(poGRider.getConnection());
    }
}

/**
* 1. Check SP Transfer Master for status = 1
* 2. Select the branches that have transfer using DISTINCT function
* 3. Select all transfers using the branch code
* 4. Check if the transaction no exists on the destination's ledger.
*       + if exists, post the transaction
*       + else, no nothing
* 5. What do we do on cancelled transfers? How can we check it?
*       + alam ko na pero hindi ko sasabihin. :P
**/

class CheckSPTransfer{
    private final String POST = "SPDl";
    private final String TRANSFER = "SPDv";
    
    private GRider poGRider;
    private String psMessage;
    
    LogWrapper logwrapr = new LogWrapper("XMGCApplication", "D:/GGC_Java_Systems/temp/CheckSPTransfer.log");
    
    public CheckSPTransfer(GRider foGRider){
        poGRider = foGRider;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public boolean PostTransactions(){
        if (poGRider == null){
            psMessage = "Application driver is not set...";
            return false;
        }
        
        if (poGRider.getUserLevel() != UserRight.ENGINEER){
            psMessage = "Only system engineers can run this utility.";
            return false;
        }
        
        String lsSQL = "SELECT" +
                            " DISTINCT(LEFT(sTransNox, 4)) xBranchCd" + 
                        " FROM SP_Transfer_Master" + 
                        " WHERE cTranStat = " + SQLUtil.toSQL(TransactionStatus.STATE_CLOSED) +
                        " ORDER BY xBranchCd";
        
        //1. Check SP Transfer Master for status = 1
        //2. Select the branches that have transfer using DISTINCT function
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        ResultSet loRSBranch;
        ResultSet loRSMaster;
        ResultSet loRSDetail;
        ResultSet loRSLedger;
        
        try {
            while (loRS.next()){
                lsSQL = "SELECT" +
                            "  sBranchCd" +
                        " FROM Branch" +
                        " WHERE sBranchCd = " + SQLUtil.toSQL(loRS.getString("xBranchCd"));
                                
                loRSBranch = poGRider.executeQuery(lsSQL);
                
                if (loRSBranch.next()){
                    //3. Select all transfers using the branch code
                    lsSQL = "SELECT *" +
                            " FROM SP_Transfer_Master" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(loRSBranch.getString("sBranchCd") + "%") + 
                                " AND cTranStat = " + SQLUtil.toSQL(TransactionStatus.STATE_CLOSED) + 
                            " ORDER BY sTransNox";
                    
                    loRSMaster = poGRider.executeQuery(lsSQL);
                    
                    while (loRSMaster.next()){
                        System.out.println(loRSMaster.getString("sTransNox"));
                        lsSQL = "SELECT *" + 
                                " FROM SP_Transfer_Detail" + 
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRSMaster.getString("sTransNox"));
                        
                        //load the detail
                        loRSDetail = poGRider.executeQuery(lsSQL);
                        
                        long lnRow = MiscUtil.RecordCount(loRSDetail);
                        long lnCtr = 0;
                        
                        while (loRSDetail.next()){
                            lsSQL = "SELECT *" +
                                    " FROM SP_Inventory_Ledger" + 
                                    " WHERE sPartsIDx = " + SQLUtil.toSQL(loRSDetail.getString("sPartsIDx")) +
                                        " AND sBranchCd = " + SQLUtil.toSQL(loRSMaster.getString("sDestinat")) + 
                                        " AND sSourceNo = " + SQLUtil.toSQL(loRSMaster.getString("sTransNox")) + 
                                        " AND sSourceCd = " + SQLUtil.toSQL(POST);
                            
                            loRSLedger = poGRider.executeQuery(lsSQL);
                            
                            if (MiscUtil.RecordCount(loRSLedger) != 0) lnCtr = lnCtr + 1;
                        }
                        
                        if (lnRow == lnCtr){
                            //we have the same detail number and the ledger entry
                            //we are safe to post the transaction now.
                            lsSQL = "UPDATE SP_Transfer_Master SET" + 
                                        "  cTranStat = " + SQLUtil.toSQL(TransactionStatus.STATE_POSTED) +
                                        ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) + 
                                        ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                                    " WHERE sTransNox = " + SQLUtil.toSQL(loRSMaster.getString("sTransNox"));
                            
                            if (poGRider.executeQuery(lsSQL, 
                                                        "SP_Transfer_Master", 
                                                        poGRider.getBranchCode(), loRSMaster.getString("sDestinat")) == 0){
                                psMessage = poGRider.getErrMsg() + ";" + poGRider.getMessage();                            
                                return false;
                            }
                            
                            logwrapr.info(lsSQL);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            psMessage = ex.getMessage();
            System.err.println(psMessage);
            return false;
        }
        
        psMessage = "POSTING of valid transaction done...";
        return true;
    }
    
    public boolean CancelTransactions(){
        if (poGRider == null){
            psMessage = "Application driver is not set...";
            return false;
        }
        
        if (poGRider.getUserLevel() != UserRight.ENGINEER){
            psMessage = "Only system engineers can run this utility.";
            return false;
        }
        
        String lsSQL = "SELECT" +
                            " DISTINCT(LEFT(sTransNox, 4)) xBranchCd" + 
                        " FROM SP_Transfer_Master" + 
                        " WHERE cTranStat = " + SQLUtil.toSQL(TransactionStatus.STATE_CLOSED) +
                        " ORDER BY xBranchCd";
        
        //1. Check SP Transfer Master for status = 1
        //2. Select the branches that have transfer using DISTINCT function
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){
                lsSQL = "SELECT" +
                            "  sBranchCd" +
                        " FROM Branch" +
                        " WHERE sBranchCd = " + SQLUtil.toSQL(loRS.getString("xBranchCd"));
                                
                ResultSet loRSBranch = poGRider.executeQuery(lsSQL);
                
                if (loRSBranch.next()){
                    //3. Select all transfers using the branch code
                    lsSQL = "SELECT *" +
                            " FROM SP_Transfer_Master" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(loRSBranch.getString("sBranchCd") + "%") + 
                                " AND cTranStat = " + SQLUtil.toSQL(TransactionStatus.STATE_CLOSED) + 
                            " ORDER BY sTransNox";
                    
                    ResultSet loRSMaster = poGRider.executeQuery(lsSQL);
                    
                    while (loRSMaster.next()){
                        System.out.println(loRSMaster.getString("sTransNox"));
                        lsSQL = "SELECT *" + 
                                " FROM SP_Transfer_Detail" + 
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRSMaster.getString("sTransNox"));
                        
                        //load the detail
                        ResultSet loRSDetail = poGRider.executeQuery(lsSQL);
                        
                        long lnRow = MiscUtil.RecordCount(loRSDetail);
                        long lnCtr = 0;
                        
                        while (loRSDetail.next()){
                            lsSQL = "SELECT *" +
                                    " FROM SP_Inventory_Ledger" + 
                                    " WHERE sPartsIDx = " + SQLUtil.toSQL(loRSDetail.getString("sPartsIDx")) +
                                        " AND sBranchCd = " + SQLUtil.toSQL(loRSBranch.getString("sBranchCd")) + 
                                        " AND sSourceNo = " + SQLUtil.toSQL(loRSMaster.getString("sTransNox")) + 
                                        " AND sSourceCd = " + SQLUtil.toSQL(TRANSFER);
                            
                            ResultSet loRSLedger = poGRider.executeQuery(lsSQL);
                            
                            if (MiscUtil.RecordCount(loRSLedger) != 0) lnCtr = lnCtr + 1;
                        }
                        
                        if (lnCtr == 0){
                            //the transfer details has no ledger
                            //we are safe to cancel the transaction now.
                            lsSQL = "UPDATE SP_Transfer_Master SET" + 
                                        "  cTranStat = " + SQLUtil.toSQL(TransactionStatus.STATE_CANCELLED) +
                                        ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) + 
                                        ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                                    " WHERE sTransNox = " + SQLUtil.toSQL(loRSMaster.getString("sTransNox"));
                            
                            if (poGRider.executeQuery(lsSQL, 
                                                        "SP_Transfer_Master", 
                                                        poGRider.getBranchCode(), "") == 0){
                                psMessage = poGRider.getErrMsg() + ";" + poGRider.getMessage();                            
                                return false;
                            }
                            
                            logwrapr.info(lsSQL);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            psMessage = ex.getMessage();
            System.err.println(psMessage);
            return false;
        }
        
        psMessage = "CACELLATION of valid transaction done...";
        return true;
    }
}
