package org.rmj.mis.util.sales;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class CPAccSalesTracker extends MCSalesTracker{
    @Override
    public boolean CreateSource() throws SQLException{
        if (p_sPeriod.isEmpty()) {
            if (MiscUtil.getDateDay(p_oApp.getServerDate()) >= 5){
                p_sPeriod = SQLUtil.dateFormat(p_oApp.getServerDate(), SQLUtil.FORMAT_SHORT_YEAR_MONTH);
            } else {
                p_sPeriod = SQLUtil.dateFormat(MiscUtil.dateAdd(p_oApp.getServerDate(), MiscUtil.getDateDay(p_oApp.getServerDate()) * -1), SQLUtil.FORMAT_SHORT_YEAR_MONTH);
            }
        }
                
        p_sSQLSource = getSQ_Source();
        p_sFldActual = getFieldActual();
        setDivision("0");
        
        return Process();
    }
    
    @Override
    public String getSQ_Source(){
        Date ldFrom = SQLUtil.toDate(p_sPeriod.substring(0, 4) + "-" + p_sPeriod.substring(4) + "-01", SQLUtil.FORMAT_SHORT_DATE);
        Date ldThru = MiscUtil.dateAdd(MiscUtil.dateAdd(ldFrom, Calendar.MONTH, 1), -1);
        
        return "SELECT a.sBranchCd" +
                    ", a.sBranchNm" +
                    ", d.sAreaCode" +
                    ", SUM((c.nUnitPrce * (100 - c.nDiscRate) / 100) - c.nDiscAmtx) xPerfrmnc" +
                " FROM Branch a" +
                    " LEFT JOIN CP_SO_Master b" +
                        " ON b.sTransNox LIKE CONCAT(a.sBranchCd, '%')" +
                            " AND b.dTransact BETWEEN " + SQLUtil.toSQL(ldFrom) +
                                " AND " + SQLUtil.toSQL(ldThru) +
                            " AND b.cTranStat <> '3'" +
                    " JOIN  CP_SO_Detail c" +
                        " ON b.sTransNox = c.sTransNox" +
                    " JOIN CP_Inventory e" +
                        " ON c.sStockIDx = e.sStockIDx" +
                    " JOIN Category f" +
                        " ON e.sCategID1 = f.sCategrID" +
                            " AND f.sCategrID NOT IN (" + SQLUtil.toSQL("C001001") + ", " + SQLUtil.toSQL("C001006") + ")" +
                    ", Branch_Others d" +
                " WHERE a.sBranchCd = d.sBranchCd" +
                    " AND d.cDivision = 0" +
                " GROUP BY a.sBranchCd";
    }    
    
    @Override
    public String getFieldActual(){
        return "nAcActual";
    }
}