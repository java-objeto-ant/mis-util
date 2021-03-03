package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.StringHelper;

public class CreateRaffleTicket {
    public static void main(String [] args){
        final String PRODUCTID = "gRider";
        final String USERID = "M001111122";
        final int FILL = 10000;
        
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
                        
        try {
            String lsSQL = "SELECT * FROM FB_Raffle_Ticket WHERE cIssuedxx = '0'";
            
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            int lnRow = (int) MiscUtil.RecordCount(loRS);
            
            lnRow = FILL - lnRow;
            
            int lnMin = 1000;
            int lnMax = 99999999;
            int lnPrv = 0;
            
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                lsSQL = "SELECT nEntryNox FROM FB_Raffle_Ticket ORDER BY nEntryNox DESC LIMIT 1";

                loRS = poGRider.executeQuery(lsSQL);

                if (loRS.next()){
                    lnPrv = loRS.getInt("nEntryNox");
                }

                boolean lbExit = false;           
                int lnAct = 0;
                String lsNew;

                do{  
                    lnAct = MiscUtil.getRandom(lnMin, lnMax);
                    lsNew = StringHelper.prepad(String.valueOf(lnAct), 8, '0');


                    lsSQL = "SELECT * FROM FB_Raffle_Ticket WHERE sRaffleNo = " + SQLUtil.toSQL(lsNew);

                    loRS = poGRider.executeQuery(lsSQL);

                    if (!loRS.next()){
                        lnPrv += 1;

                        lsSQL = "INSERT INTO FB_Raffle_Ticket SET" +
                                    "  nEntryNox = " + lnPrv +
                                    ", sRaffleNo = " + SQLUtil.toSQL(lsNew) +
                                    ", cIssuedxx = '0'";

                        if (poGRider.executeUpdate(lsSQL) <= 0){
                            System.err.println(poGRider.getErrMsg() + ";" + poGRider.getMessage());
                            System.exit(1);
                        }

                        lbExit = true;
                    }
                }while(!lbExit);  
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
