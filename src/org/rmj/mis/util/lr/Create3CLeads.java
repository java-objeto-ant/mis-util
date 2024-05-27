package org.rmj.mis.util.lr;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.StringHelper;

public class Create3CLeads {
    final int REFILL = 100;
    
    GRider oApp;
    ResultSet oRS;
    String sMessage;
    String dDateFrom;
    String dDateThru;
    
    public Create3CLeads(GRider foApp){
        oApp = foApp;
        
        dDateFrom = "";
        dDateThru = "";
        sMessage = "";
    }
    
    public boolean Create(){
        int lnDaysDlyd;
        int ln2Refillx;
        int lnRefilled;
        String lsDueDatex;
        
        //add to leads cx that is unpaid 3 days after due date up to 1 month
        String lsSQL = getSQ_Accounts();
        
        //get accounts that has delay of 1 to 2
        oRS = oApp.executeQuery(lsSQL);
        
        try {
           lnRefilled = 0;
           ln2Refillx = get2Fill();
            while(oRS.next()){
                if (lnRefilled >= ln2Refillx) break;
                
                //customer has amount due
                if (oRS.getDouble("nAmtDuexx") > 0.00){
                    lsDueDatex = String.valueOf(CommonUtils.getDateYear(oApp.getServerDate())) + "-" +
                                    StringHelper.prepad(String.valueOf(CommonUtils.getDateMonth(oApp.getServerDate())), 2, '0') + "-" +
                                    StringHelper.prepad(String.valueOf(CommonUtils.getDateDay(SQLUtil.toDate(oRS.getString("dDueDatex"), SQLUtil.FORMAT_SHORT_DATE))), 2, '0');
                    
                    //compute # of days delayed
                    lnDaysDlyd = (int) CommonUtils.dateDiff(oApp.getServerDate(), SQLUtil.toDate(lsDueDatex, SQLUtil.FORMAT_SHORT_DATE));
                    System.out.println(oRS.getString("sAcctNmbr") + "\t" + 
                                        oRS.getDouble("nMonAmort") + "\t" +
                                        oRS.getDouble("nAmtDuexx") + "\t" + 
                                        oRS.getDouble("nDelayAvg") + "\t" +
                                        lsDueDatex + "\t" +
                                        lnDaysDlyd);
                    
                    //customer is delayed 3 days after the due date
                    if (oRS.getDouble("nDelayAvg") > 1 || lnDaysDlyd >= 3){
                        oApp.beginTrans();
                        
                        //delete for collection
                        lsSQL = "DELETE FROM LR_Collection_Unit WHERE sAcctNmbr = " + SQLUtil.toSQL(oRS.getString("sAcctNmbr"));
                        
                        oApp.executeUpdate(lsSQL);

                        //create for collection
                        lsSQL = "INSERT INTO LR_Collection_Unit SET" +
                                "  sAcctNmbr = " + SQLUtil.toSQL(oRS.getString("sAcctNmbr")) +
                                ", sBranchCd = " + SQLUtil.toSQL(oRS.getString("sBranchCd")) +
                                ", dTransact = " + SQLUtil.toSQL(oApp.getServerDate()) +
                                ", cCollUnit = '0'" +
                                ", sInCharge = " + SQLUtil.toSQL("") +
                                ", cApntUnit = " + SQLUtil.toSQL("") +
                                ", dDueUntil = " + SQLUtil.toSQL(CommonUtils.dateAdd(oApp.getServerDate(), 5)) +
                                ", cCollStat = '0'" +
                                ", nPriority = 1" + 
                                ", nNoSchedx = 0" +
                                ", sModified = 'M001111122'" + 
                                ", dModified = " + SQLUtil.toSQL(oApp.getServerDate());
                        
                        if (oApp.executeUpdate(lsSQL) <= 0){
                            oApp.rollbackTrans();
                            sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                            return false;
                        }

                        //add customer to leads
                        lsSQL = "INSERT INTO LR_Calls_Master SET" +
                                "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("LR_Calls_Master", "sTransNox", true, oApp.getConnection(), oApp.getBranchCode())) +
                                ", dTransact = " + SQLUtil.toSQL(oApp.getServerDate()) +
                                ", sAcctNmbr = " + SQLUtil.toSQL(oRS.getString("sAcctNmbr")) +
                                ", sRemarksx = ''" +
                                ", cTranStat = '0'" +
                                ", sAsgAgent = ''" +
                                ", sAgentIDx = ''" +
                                ", sModified = 'M001111122'" + 
                                ", dModified = " + SQLUtil.toSQL(oApp.getServerDate());
                                
                        if (oApp.executeUpdate(lsSQL) <= 0){
                            oApp.rollbackTrans();
                            sMessage = oApp.getErrMsg() + "; " + oApp.getMessage();
                            return false;
                        }

                        //update for collection
                        lsSQL = "UPDATE LR_Collection_Unit SET" +
                                    "  dScheduld = " + SQLUtil.toSQL(oApp.getServerDate()) +
                                    ", sInCharge = ''" +
                                    ", cCollStat = '1'" + 
                                " WHERE sAcctNmbr = " + SQLUtil.toSQL(oRS.getString("sAcctNmbr")) +
                                    " AND cCollUnit = '0'";
                        
                        oApp.executeUpdate(lsSQL);
                        
                        oApp.commitTrans();
                        
                        lnRefilled += 1;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sMessage = e.getMessage();
            return false;
        }

        return true;
    }
    
    private String getSQ_Accounts(){
        return "SELECT" + 
                    "  CONCAT(j.sFrstName, ' ', j.sLastName) xMasGroup" +
                    ", a.sAcctNmbr" +
                    ", CONCAT(b.sLastName, ', ', b.sFrstName, ' ', LEFT(b.sMiddName, 1)) AS xCustName" +
                    ", b.sAddressx" +
                    ", x.sBrgyName" +
                    ", e.sProvName" +
                    ", d.sTownName" +
                    ", a.nGrossPrc" +
                    ", a.nMonAmort" +
                    ", a.nDownPaym" +
                    ", a.nCashBalx" +
                    ", a.dFirstPay" +
                    ", a.dPurchase" +
                    ", a.dLastPaym" +
                    ", a.nLastPaym" +
                    ", a.nPaymTotl" +
                    ", a.dDueDatex" +
                    ", a.nAmtDuexx" +
                    ", a.nDelayAvg" +
                    ", g.sModelNme" +
                    ", a.nAcctTerm" +
                    ", a.nDownTotl" +
                    ", a.nCashTotl" +
                    ", a.nCredTotl" +
                    ", a.nDebtTotl" +
                    ", a.nRebTotlx" +
                    ", a.cRatingxx" +
                    ", v.sAreaDesc" +
                    ", z.sBranchNm" +
                    ", w.sCompnyNm sCollectx" +
                    ", h.sEngineNo" + 
                    ", k.sTransNox xTransNox" +
                    ", a.sBranchCd" +
                " FROM MC_AR_Master a" + 
                    " LEFT JOIN MC_Serial h ON a.sSerialID = h.sSerialID" + 
                    " LEFT JOIN MC_Model g ON h.sModelIDx = g.sModelIDx" +
                    " LEFT JOIN LR_Calls_Master k ON a.`sAcctNmbr` = k.sAcctNmbr AND k.dTransact BETWEEN " + SQLUtil.toSQL(dDateFrom) + " AND " + SQLUtil.toSQL(dDateThru) +
                    ", Client_Master b" + 
                    " LEFT JOIN Barangay x ON b.sBrgyIDxx = x.sBrgyIDxx" +
                    ", Route_Area c" + 
                    " LEFT JOIN Branch z ON c.sBranchCd = z.sBranchCd" + 
                    " LEFT JOIN Branch_Others y ON z.sBranchCd = y.sBranchCd" + 
                    " LEFT JOIN Branch_Area v ON y.sAreaCode = v.sAreaCode" + 
                    " LEFT JOIN Client_Master w ON c.sCollctID = w.sClientID" +
                    ", TownCity d" +
                    ", Province e" +
                    ", Employee_Master001 f" + 
                    " LEFT JOIN Client_Master j ON f.sEmployID = j.sClientID" + 
                " WHERE a.sClientID = b.sClientID" + 
                    " AND a.sRouteIDx = c.sRouteIDx" + 
                    " AND b.sTownIDxx = d.sTownIDxx" + 
                    " AND d.sProvIDxx = e.sProvIDxx" + 
                    " AND f.sEmployID = c.sCollctID" + 
                    " AND a.dPurchase < " + SQLUtil.toSQL(dDateFrom + " 00:00:00") + 
                    " AND a.dLastPaym > DATE_SUB(NOW(), INTERVAL 3 MONTH)" +
                    " AND (a.cAcctStat = '0' OR (a.dClosedxx >= " + SQLUtil.toSQL(dDateFrom + " 00:00:00") + " AND a.cAcctStat <> '0'))" + 
                    " AND a.cMotorNew = '1' " +
                    " AND a.nDelayAvg BETWEEN 1 AND 2" +
                " HAVING xTransNox IS NULL" +
                " ORDER BY DAY(a.dDueDatex)";
        
        //" AND a.cLoanType <> '4'" +
    }
    
    public int get2Fill(){
        String lsSQL = "SELECT sTransNox FROM LR_Calls_Master" +
                        " WHERE dTransact >= '2023-07-22'" +
                            " AND cTranStat = '0'";
        
        return REFILL - (int) MiscUtil.RecordCount(oApp.executeQuery(lsSQL));
    }
    
    public void setDateFrom(String fsValue){
        dDateFrom = fsValue;
    }
    
    public void setDateThru(String fsValue){
        dDateThru = fsValue;
    }
    
    public String getMessage(){
        return sMessage;
    }
}
