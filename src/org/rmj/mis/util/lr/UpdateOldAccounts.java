package org.rmj.mis.util.lr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class UpdateOldAccounts {
    public static void main (String [] args){
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
        
//        String lsSQL = "SELECT a.sAcctNmbr, a.dModified, b.nEntryNox, b.dTransact, b.cTrantype, b.sRemarksx, b.nABalance" +
//                        " FROM MC_AR_Master a" +
//                            " LEFT JOIN MC_AR_Ledger b" +
//                                " ON a.sAcctNmbr = b.sAcctNmbr" +
//                                    " AND a.nLedgerNo = b.nEntryNox" +
//                        " WHERE a.cAcctStat = '0'" +
//                            " AND a.dModified BETWEEN '2021-06-01 06:00:00' AND '2021-06-16 23:00:00'" +
//                            " AND b.dTransact < '2021-01-01'" +
//                        " HAVING b.cTrantype IN ('p') AND b.nABalance <= 0.00" +
//                        " ORDER BY b.dTransact";

        String lsSQL = "SELECT a.sAcctNmbr, a.dPurchase, a.dModified, b.nEntryNox, b.dTransact, b.cTrantype, b.sRemarksx, b.nABalance" +
                        " FROM MC_AR_Master a" +
                            " LEFT JOIN MC_AR_Ledger b" +
                                " ON a.sAcctNmbr = b.sAcctNmbr" +
                                    " AND a.nLedgerNo = b.nEntryNox" +
                        " WHERE a.cAcctStat = '0'" +
                            " AND a.dModified BETWEEN '2021-06-16 11:00:00' AND '2021-06-16 23:00:00'" +
                        " HAVING a.dPurchase <= '2002-01-10'" +
                        " ORDER BY b.dTransact";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            poGRider.beginTrans();
            while (loRS.next()){
                lsSQL = "UPDATE MC_AR_Master SET" +
                            "  cAcctStat = '1'" +
                            ", dClosedxx = " + SQLUtil.toSQL(loRS.getString("dTransact")) +
                        " WHERE sAcctNmbr = " + SQLUtil.toSQL(loRS.getString("sAcctNmbr"));
                poGRider.executeQuery(lsSQL, "MC_AR_Master", "", "");
            }
            poGRider.commitTrans();
        } catch (SQLException ex) {
            poGRider.rollbackTrans();
            ex.printStackTrace();
        }
    }  
}

