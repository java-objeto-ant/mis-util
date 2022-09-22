package org.rmj.mis.util.sales;

import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class CPUnitSalesTracker extends MCSalesTracker{
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
        return "SELECT a.sBranchCd" +
                    ", a.sBranchNm" +
                    ", d.sAreaCode" +
                    ", SUM((c.nUnitPrce * (100 - c.nDiscRate) / 100) - c.nDiscAmtx) xPerfrmnc" +
                " FROM Branch a" +
                    " LEFT JOIN CP_SO_Master b" +
                        " ON b.sTransNox LIKE CONCAT(a.sBranchCd, '%')" +
                            " AND b.cTranStat <> '3'" +
                            " AND DATE_FORMAT(b.dTransact, '%Y%m') = " + SQLUtil.toSQL(getPeriod()) +
                    " JOIN  CP_SO_Detail c" +
                        " ON b.sTransNox = c.sTransNox" +
                            " AND c.sSerialID <> ''" +
                    ", Branch_Others d" +
                " WHERE a.sBranchCd = d.sBranchCd" +
                    " AND d.cDivision = 0" +
                " GROUP BY a.sBranchCd";
    }    
    
    @Override
    public String getFieldActual(){
        return "nCPActual";
    }
}