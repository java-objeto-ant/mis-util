package org.rmj.mis.util.raffle;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.StringHelper;

/**
 * @author mac
 * @since 2022.09.30
 */
public class ExtractLR extends ExtractMPSales{
    private final String SOURCECD = "LRxx";
    
    @Override
    public boolean Run() {
        String lsSQL = getSQ_Master();
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) == 0) return true;
        
        try {
            String lsDivision = getDivision(); //branch division
            String lsTransNox;
            
            int lnLstRflNo = getLastRaffleNo(lsDivision); //last used raffle number
            int lnTmpRflNo = lnLstRflNo; //incrementing raffle number
            
            int lnNoEntryx; //number of entries
            int lnRandNmbr; //random number
            int lnRfleFrom; //raffle from
            int lnRfleThru; //raffle thru
            
            while (loRS.next()){
                //ilan ang ibibigay na raffle entry?
                lnNoEntryx = getEntryNo(loRS.getDouble("nTranAmtx"), loRS.getDouble("nDiscount"), loRS.getDouble("nMonAmort"));
                lnRandNmbr = MiscUtil.getRandom(0, 99); 
                
                if (lnNoEntryx > 0){
                    lnRfleFrom = lnTmpRflNo + 1;
                    lnRfleThru = lnRfleFrom;
                    
                    instance.beginTrans();
                    
                    lsTransNox = MiscUtil.getNextCode("Raffle_With_SMS_Source", "sTransNox", true, instance.getConnection(), instance.getBranchCode());
                    
                    for (int lnCtr = 1; lnCtr <= lnNoEntryx; lnCtr++){                       
                        lnTmpRflNo += 1;
                        lnRfleThru = lnTmpRflNo;
                        
                        lsSQL = "INSERT INTO Raffle_With_SMS_Entry SET" +
                                    "  sTransNox = " + SQLUtil.toSQL(lsTransNox) +
                                    ", sRaffleNo = " + SQLUtil.toSQL(getRaffleNo(lsDivision, lnRandNmbr, lnTmpRflNo)) +
                                    ", cRaffledx  = '0'";                       
                        
                        if (instance.executeQuery(lsSQL, "Raffle_With_SMS_Entry", instance.getBranchCode(), sBranchCd) <= 0){
                            instance.rollbackTrans();
                            setMessage(instance.getErrMsg() + "; " + instance.getMessage());
                            return false;
                        }
                    }
                    
                    lsSQL = "INSERT INTO Raffle_With_SMS_Source SET" +
                                "  sTransNox = " + SQLUtil.toSQL(lsTransNox) +
                                ", dTransact = " + SQLUtil.toSQL(loRS.getString("dTransact")) +
                                ", sBranchCd = " + SQLUtil.toSQL(sBranchCd) +
                                ", sSourceCd = " + SQLUtil.toSQL(SOURCECD) +
                                ", sSourceNo = " + SQLUtil.toSQL(loRS.getString("sTransNox")) +
                                ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sReferNox")) +
                                ", sAcctNmbr = " + SQLUtil.toSQL(loRS.getString("sAcctNmbr")) +
                                ", sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                                ", sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                                ", cDivision = " + SQLUtil.toSQL(lsDivision) +
                                ", sRandomNo = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(lnRandNmbr), 2, '0')) +
                                ", sRaffleFr = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(lnRfleFrom), 9, '0')) +
                                ", sRaffleTr = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(lnRfleThru), 9, '0')) +
                                ", nNoEntryx = " + lnNoEntryx;
                    
                    if (instance.executeQuery(lsSQL, "Raffle_With_SMS_Source", instance.getBranchCode(), sBranchCd) <= 0){
                        instance.rollbackTrans();
                        setMessage(instance.getErrMsg() + "; " + instance.getMessage());
                        return false;
                    }
                    instance.commitTrans();
                }
            }
        } catch (SQLException e) {
            setMessage(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    private int getEntryNo(double fnTranAmtx, double fnDiscount, double fnMonAmort){
        return (int) Math.round((fnTranAmtx + fnDiscount) / fnMonAmort);
    }
    
    @Override
    protected String getSQ_Master(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", b.sTransNox sRaffleID" +
                    ", a.dTransact" +
                    ", LEFT(a.sTransNox, 4) sBranchCd" +
                    ", e.sClientID" +
                    ", a.sAcctNmbr" +
                    ", c.sMobileNo" +
                    ", a.sReferNox sReferNox" +
                    ", 'LRxx' sSourceCd" +
                    ", '0' cRaffledx" +
                    ", a.sModified" +
                    ", a.dModified" +
                    ", a.nAmountxx nTranAmtx" +
                    ", a.nRebatesx nDiscount" +
                    ", e.nMonAmort" +
                " FROM LR_Payment_Master a" + 
                    " LEFT JOIN Raffle_With_SMS_Source b" + 
                        " ON LEFT(a.sTransNox, 4) = b.sBranchCd" + 
                            " AND a.sTransNox = b.sSourceNo" + 
                            " AND b.sSourceCd = " + SQLUtil.toSQL(SOURCECD) +
                    " LEFT JOIN Client_Master c" + 
                        " ON a.sClientID = c.sClientID" + 
                    " LEFT JOIN Employee_Master001 d" + 
                        " ON a.sClientID = d.sEmployID" + 
                    " LEFT JOIN MC_AR_Master e" + 
                        " ON a.sAcctNmbr = e.sAcctNmbr" + 
                " WHERE a.sTransNox LIKE " + SQLUtil.toSQL(sBranchCd + "%")+ 
                    " AND a.dTransact BETWEEN " + SQLUtil.toSQL(FROM_DATE) + " AND " + SQLUtil.toSQL(THRU_DATE) +
                    " AND a.cTranType = '2'" + 
                    " AND a.cPostedxx = 2" + 
                    " AND d.sEmployID IS NULL" + 
                    " AND b.sTransNox IS NULL" + 
                    " AND IFNULL(e.nMonAmort, 0) > 0" + 
                    " AND LENGTH(c.sMobileNo) BETWEEN 11 AND 13" + 
                " ORDER BY dTransact, sReferNox";
    }
}
