package org.rmj.mis.util.raffle;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.StringHelper;

/**
 * @author mac
 * @since 2022.10.03
 */
public class ExtractOtherDivision extends ExtractMPSales{    
    @Override
    public boolean Run() {
        String lsSQL = getSQ_Master();
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) == 0) return true;
        
        try {
            String lsDivision;
            String lsTransNox;
            
            int lnLstRflNo;
            int lnTmpRflNo;
            
            int lnNoEntryx; //number of entries
            int lnRandNmbr; //random number
            int lnRfleFrom; //raffle from
            int lnRfleThru; //raffle thru
            
            while (loRS.next()){
                //ilan ang ibibigay na raffle entry?
                lnNoEntryx = loRS.getInt("nNoEntryx");
                lnRandNmbr = MiscUtil.getRandom(0, 99); 
                
                lsDivision = getDivision(loRS.getString("sBranchCd"));
                lnLstRflNo = getLastRaffleNo(lsDivision); //last used raffle number
                lnTmpRflNo = lnLstRflNo; //incrementing raffle number
                
                if (lnNoEntryx > 0){
                    lnRfleFrom = lnTmpRflNo + 1;
                    lnRfleThru = lnRfleFrom;
                    
                    instance.beginTrans();
                    
                    lsTransNox = loRS.getString("sTransNox");
                    
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
                    
                    lsSQL = "UPDATE Raffle_With_SMS_Source SET" +
                                "  cDivision = " + SQLUtil.toSQL(lsDivision) +
                                ", sRandomNo = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(lnRandNmbr), 2, '0')) +
                                ", sRaffleFr = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(lnRfleFrom), 9, '0')) +
                                ", sRaffleTr = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(lnRfleThru), 9, '0')) +
                                ", cRaffledx = '0'" +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    
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
    
    protected String getDivision(String fsBranchCd) throws SQLException{
        String lsSQL = "SELECT cDivision FROM Branch_Others WHERE sBranchCd = " + SQLUtil.toSQL(fsBranchCd);
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        lsSQL = "";
        
        if (loRS.next()) lsSQL = loRS.getString("cDivision");
        
        return lsSQL;
    }
    
    @Override
    protected String getSQ_Master(){
        return "SELECT" +
                    "  sTransNox" +
                    ", dTransact" +
                    ", sBranchCd" +
                    ", sSourceCd" +
                    ", sSourceNo" +
                    ", sReferNox" +
                    ", sAcctNmbr" +
                    ", sClientID" +
                    ", sClientNm" +
                    ", sAddressx" +
                    ", sMobileNo" +
                    ", cDivision" +
                    ", sRandomNo" +
                    ", sRaffleFr" +
                    ", sRaffleTr" +
                    ", nNoEntryx" +
                    ", cMsgSentx" +
                    ", cCltCnfrm" +
                    ", cSysCnfrm" +
                    ", cRaffledx" +
                " FROM Raffle_With_SMS_Source" + 
                " WHERE cRaffledx IS NULL" +
                " ORDER BY dTransact";
    }
}
