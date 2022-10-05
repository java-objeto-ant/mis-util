package org.rmj.mis.util.raffle;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.StringHelper;

/**
 * @author mac
 * @since 2022.09.29
 */
public class ExtractMPSales implements RaffleValidator{
    private final String SOURCECD = "MPSl";
    protected final String FROM_DATE = "2022-08-01 00:00:00";
    protected final String THRU_DATE = "2022-08-31 23:59:30";

    protected GRiderX instance;
    protected String sBranchCd;
    
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
    public void setBranch(String fsValue) {
        sBranchCd = fsValue;
        
        if (sBranchCd.isEmpty()){
            System.err.println("Branch is not set.");
            System.exit(1);
        }
    }

    //inherit
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
                lnNoEntryx = getEntryNo(loRS.getDouble("nTranAmtx"));
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
                                ", sAcctNmbr = ''" + 
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

    @Override
    public void setMessage(String fsValue) {
        sMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return sMessage;
    }
    
    protected String getDivision() throws SQLException{
        String lsSQL = "SELECT cDivision FROM Branch_Others WHERE sBranchCd = " + SQLUtil.toSQL(sBranchCd);
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        lsSQL = "";
        
        if (loRS.next()) lsSQL = loRS.getString("cDivision");
        
        return lsSQL;
    }
    
    /*
        RAFFLE NO: 	
            DXXNNNNNNNNN
        WHERE: 	
            D -> Division
            XX -> Random Number
            NNNNNNNNN -> Series Number
    */
    protected String getRaffleNo(String fsDivision, int fnRandNmbr, int fnSeriesNo){        
        return fsDivision + 
            StringHelper.prepad(String.valueOf(fnRandNmbr), 2, '0') + 
            StringHelper.prepad(String.valueOf(fnSeriesNo), 9, '0');
    }
    
    protected int getLastRaffleNo(String fsDivision) throws SQLException{
        String lsSQL = "SELECT sRaffleTr" +
                        " FROM Raffle_With_SMS_Source" +
                        " WHERE cDivision = " + SQLUtil.toSQL(fsDivision) +
                        " ORDER BY sRaffleFr DESC LIMIT 1";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        if (loRS.next())
            return Integer.parseInt(loRS.getString("sRaffleTr"));
        else 
            return 0;
    }
    
    protected int getEntryNo(double fnValue){
        if (isBetween(fnValue, 0.00, 1000.00)){
            return 1;
        } else if (isBetween(fnValue, 1000.01, 10000.00)){
            return 2;
        } else
            return 3;
    }
    
    private boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x <= upper;
    }
    
    protected String getSQ_Master(){
        return "SELECT" + 
                    "  a.sTransNox" +
                    ", b.sTransNox sRaffleID" +
                    ", a.dTransact" +
                    ", LEFT(a.sTransNox, 4) sBranchCd" +
                    ", a.sClientID" +
                    ", c.sMobileNo" +
                    ", a.sSalesInv sReferNox" +
                    ", 'MPSl' sSourceCd" +
                    ", '0' cRaffledx" +
                    ", a.sModified" +
                    ", a.dModified" +
                    ", a.nAmtPaidx" + //org nTranAmtx field
                    ", d.nUnitPrce - (d.nUnitPrce * d.nDiscRate / 100) - d.nDiscAmtx nTranAmtx" +
                " FROM CP_SO_Master a" + 
                    " LEFT JOIN CP_SO_Detail d" + 
                        " ON a.sTransNox = d.sTransNox" + 
                    " LEFT JOIN Raffle_With_SMS_Source b" + 
                        " ON LEFT(a.sTransNox, 4) = b.sBranchCd" + 
                            " AND a.sTransNox = b.sSourceNo" + 
                            " AND b.sSourceCd = " + SQLUtil.toSQL(SOURCECD) +
                    " LEFT JOIN Client_Master c" + 
                        " ON a.sClientID = c.sClientID" + 
                    " LEFT JOIN Employee_Master001 e" + 
                        " ON a.sClientID = e.sEmployID" + 
                " WHERE a.sTransNox LIKE " + SQLUtil.toSQL(sBranchCd + "%")+ 
                    " AND a.dTransact BETWEEN " + SQLUtil.toSQL(FROM_DATE) + " AND " + SQLUtil.toSQL(THRU_DATE) +
                    " AND d.sSerialID <> ''" + 
                    " AND d.nUnitPrce > 0.00" +
                    " AND NOT (a.cTranStat & '3' = 3)" + 
                    " AND e.sEmployID IS NULL" + 
                    " AND b.sTransNox IS NULL" + 
                    " AND a.sSalesInv <> ''" + 
                    " AND LENGTH(c.sMobileNo) BETWEEN 11 AND 13" + 
                " ORDER BY dTransact, sReferNox";
    }
}
