package org.rmj.mis.util.lr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class GetUpaid {
    public static void main(String [] args){
        final String PRODUCTID = "IntegSys";
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
        
        //get the account information
        String lsSQL = "SELECT" +
                            "  d.`sAreaDesc`" +
                            ", b.`sBranchNm`" +
                            ", a.`sAcctNmbr`" +
                            ", CONCAT(e.sLastName, ', ', e.sFrstName) sCompnyNm" +
                            ", g.`sModelNme`" +
                            ", a.`nMonAmort`" +
                            ", a.`dLastPaym`" +
                            ", a.`nLastPaym`" +
                        " FROM MC_AR_Master a" +
                                " LEFT JOIN Branch b" +
                                    " ON a.`sBranchCd` = b.`sBranchCd`" +
                                " LEFT JOIN `Branch_Others` c" +
                                    " ON b.`sBranchCd` = c.`sBranchCD`" +
                                " LEFT JOIN `Branch_Area` d" +
                                    " ON c.`sAreaCode` = d.`sAreaCode`" +
                            ", Client_Master e" +
                            ", MC_Serial f" +
                                " LEFT JOIN MC_Model g" +
                                    " ON f.`sModelIDx` = g.`sModelIDx`" +
                    " WHERE a.`sClientID` = e.`sClientID`" +
                        " AND a.`sSerialID` = f.`sSerialID`" +
                        " AND a.`cLoanType` = '0'" +
                        " AND a.`cAcctstat` = '0' " +
                        " AND IFNULL(a.`cActTypex`, '0') = '0'" +
                        " AND IFNULL(a.`cMCStatxx`, '0') = '0'" +
                        " AND a.`dPurchase` < '2020-03-01'";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            ResultSet loPaym;            
            String[][] lasPay = new String[1][2];
            
            int lnRow;
            int lnCtr;
            
            //check payments
            while (loRS.next()){
                //did he pay from period between March 1, 2020 to July 31, 2020?
                lsSQL = "SELECT * FROM MC_AR_Ledger" +
                        " WHERE sAcctNmbr = " + SQLUtil.toSQL(loRS.getString("sAcctNmbr")) +
                            " AND dTransact BETWEEN '2020-03-01' AND '2020-07-31'";
                
                loPaym = poGRider.executeQuery(lsSQL);
                
                //no payment
                if (MiscUtil.RecordCount(loPaym) == 0){
                    System.out.print(loRS.getString("sAreaDesc") + "\t" + loRS.getString("sBranchNm") + "\t");
                    System.out.print(loRS.getString("sAcctNmbr") + "\t" + loRS.getString("sCompnyNm") + "\t");
                    System.out.print(loRS.getString("sModelNme") + "\t" + loRS.getString("nMonAmort") + "\t");
                    
                    lsSQL = "SELECT * FROM MC_AR_Ledger" +
                            " WHERE sAcctNmbr = " + SQLUtil.toSQL(loRS.getString("sAcctNmbr")) +
                                " AND dTransact BETWEEN '2020-08-01' AND '2020-08-31'";
                                
                    loPaym = poGRider.executeQuery(lsSQL);
                    
                    lnRow = (int) MiscUtil.RecordCount(loPaym);
                    
                    //customer paid on August 2020
                    if (lnRow > 0){
                        lasPay = new String [lnRow][2];
                        lnCtr = 0;
                        
                        System.out.print("YES" + "\t");
                        
                        while (loPaym.next()){
                            lasPay[lnCtr][0] = SQLUtil.dateFormat(loPaym.getDate("dTransact"), SQLUtil.FORMAT_SHORT_DATE);
                            lasPay[lnCtr][1] = String.valueOf(loPaym.getDouble("nTranAmtx") + loPaym.getDouble("nRebatesx") + loPaym.getDouble("nOthersxx"));
                            
                            lnCtr += 1;
                        }
                        
                        lnCtr = 0;
                        while (lnCtr < lasPay.length){
                            System.out.print(lasPay[lnCtr][0] + "\t" + lasPay[lnCtr][1] + "\t");
                            lnCtr += 1;
                            
                            if (lnCtr == lasPay.length) 
                                System.out.print("\n");
                        }
                    } else {
                        System.out.print("NO");
                        System.out.print("\n");
                    }       
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(GetUpaid.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
