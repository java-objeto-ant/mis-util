package org.rmj.mis.util.atm;

import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;

public class ATMHotline {
    private static String SOURCECD = "('ILMJ', 'grtB', 'acmp', 'remP', 'ackM', 'tksm', 'MCAP', 'MPAP', 'FSEP', 'FSEC', 'ApCd', 'remJ', 'TLMH', 'GCRd', 'GCAc', 'SMS0', 'CODE')";
    
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
        final String USERID = "M001111122";
        
        GRider poGRider = new GRider(PRODUCTID);

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT" +
                            "  sTransNox" +
                            ", dTransact" +
                            ", sDivision" +
                            ", sMobileNo" +
                            ", sMessagex" +
                            ", cSubscrbr" +
                            ", dDueUntil" +
                            ", cSendStat" +
                            ", nPriority" +
                            ", sUDHeader" +
                            ", sSourceCd" +
                            ", cTranStat" +
                        " FROM  HotLine_Outgoing" +
                        " WHERE dDueUntil >=  " + SQLUtil.toSQL(SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_SHORT_DATE)) +
                            " AND sSourceCd IN " + SOURCECD +
                            " AND cSendStat =  '0'" +
                            " AND cTranStat <> '3'" +
                            " AND nNoRetryx <= 3" +
                        " ORDER BY nPriority, sSourceCd, dDueUntil, sTransNox";
        
        System.out.println(lsSQL);
    }
}
