package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class MPISys2CASys {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("MPISys2CASys", "mis.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX maindb = new GRiderX("gRider");
        
        if (!maindb.logUser("gRider", "M001111122")){
            logwrapr.severe(maindb.getMessage() + maindb.getErrMsg());
            System.exit(1);
        }
        
        GRiderX posdb = new GRiderX("Telecom");
        
        if (!posdb.logUser("Telecom", "M001111122")){
            logwrapr.severe(posdb.getMessage() + posdb.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT" +
                            "  sTransNox" +
                            ", dTransact" +
                            ", sSalesInv" +
                            ", sApproved" +
                            ", sRemarksx" +
                            ", nTranTotl" +
                            ", nAmtPaidx" +
                            ", sTermIDxx" +
                            ", cPaymForm" +
                            ", nReplAmtx" +
                            ", cTranStat" +
                            ", sClientID" +
                            ", sCashierx" +
                            ", sORNoxxxx" +
                            ", sSalesman" +
                            ", sAddedByx" +
                            ", dAddedDte" +
                            ", sReferNox" +
                            ", sSourceCd" +
                            ", sModified" +
                            ", dModified" +
                        " FROM CP_SO_Master" +
                        " WHERE sSourceCd = 'POS'" + 
                            " AND sReferNox LIKE 'C103%'";
        
        ResultSet loRS = maindb.executeQuery(lsSQL);
        
        try {
            //if (!transferClient(loRS, maindb, posdb)) System.exit(1);
            if (!transferSales(loRS, maindb, posdb)) System.exit(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        insertDailySales(posdb);
                
        logwrapr.info("Thank you.");
        System.exit(0);
    }
    
    private static boolean insertDailySales(GRiderX posdb){
        String lsSQL = "SELECT DATE('2022-01-21') + INTERVAL n DAY AS dt" +
                        " FROM (" +
                                " SELECT a.N + b.N * 10 + c.N * 100 + d.N * 1000 AS n" +
                                " FROM" + 
                                    "(SELECT 0 N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4" +
                                    " UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a," +
                                    "(SELECT 0 N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4" +
                                    " UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b," +
                                    "(SELECT 0 N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4" +
                                    " UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c," +
                                    "(SELECT 0 N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4" +
                                    " UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d" +
                                ") numbers" +
                        " WHERE DATE('2022-01-21') + INTERVAL n DAY <= '2023-09-17'";
        
        ResultSet loRS = posdb.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){
                lsSQL = "SELECT * FROM Daily_Summary WHERE sTranDate = " + SQLUtil.toSQL(SQLUtil.dateFormat(loRS.getDate("dt"), SQLUtil.FORMAT_SHORT_DATEX));
                
                ResultSet loRx = posdb.executeQuery(lsSQL);
                
                if (!loRx.next()){
                    //insert daily sales
                    lsSQL = "INSERT INTO Daily_Summary SET" +
                                "  sTranDate = " + SQLUtil.toSQL(SQLUtil.dateFormat(loRS.getDate("dt"), SQLUtil.FORMAT_SHORT_DATEX)) +
                                ", sCRMNumbr = '21112317242080606'" +
                                ", sCashierx = 'M001111122'" +
                                ", nOpenBalx = 0.00" +
                                ", nCPullOut = 0.00" +
                                ", nSalesAmt = 0.00" +
                                ", nVATSales = 0.00" +
                                ", nVATAmtxx = 0.00" +
                                ", nNonVATxx = 0.00" +
                                ", nZeroRatd = 0.00" +
                                ", nDiscount = 0.00" +
                                ", nVatDiscx = 0.00" +	
                                ", nPWDDiscx = 0.00" +	
                                ", nReturnsx = 0.00" +	
                                ", nVoidAmnt = 0.00" +	
                                ", nAccuSale = 0.00" +	
                                ", nCashAmnt = 0.00" +	
                                ", nChckAmnt = 0.00" +	
                                ", nCrdtAmnt = 0.00" +	
                                ", nChrgAmnt = 0.00" +	
                                ", nGiftAmnt = '0.00'" +	
                                ", nFinAmntx = 0.00" +	
                                ", sORNoFrom = ''" +
                                ", sORNoThru = ''" +
                                ", dOpenedxx = " + SQLUtil.toSQL(loRS.getDate("dt")) +
                                ", cTranStat = '0'";
                    System.out.println(lsSQL);
                    posdb.executeUpdate(lsSQL);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    private static boolean transferSales(ResultSet trans, GRiderX maindb, GRiderX posDB) throws SQLException{
        trans.beforeFirst();
        
        posDB.beginTrans();
        while(trans.next()){
            String lsSalesTrans = trans.getString("sReferNox");
            double lnItemTotal = 0.00;
            
            String lsSQL = "SELECT * FROM Client_Master WHERE sClientID = " + SQLUtil.toSQL(trans.getString("sReferNox"));
                
            ResultSet loRx = posDB.executeQuery(lsSQL);

            //client has no record in the pos database
            if (!loRx.next()){                
                //transfer master record
                lsSQL = "INSERT INTO Sales_Master SET" +
                        "  sTransNox = " + SQLUtil.toSQL(lsSalesTrans) +
                        ", sBranchCd = " + SQLUtil.toSQL(posDB.getBranchCode()) +
                        ", dTransact = " + SQLUtil.toSQL(trans.getDate("dTransact")) +
                        ", nAmtPaidx = " + SQLUtil.toSQL(0.00) +
                        ", sApproved = " + SQLUtil.toSQL(trans.getString("sApproved")) +
                        ", sRemarksx = " + SQLUtil.toSQL(trans.getString("sRemarksx")) +
                        ", nTranTotl = " + SQLUtil.toSQL(0.00) +
                        ", sTermCode = " + SQLUtil.toSQL(trans.getString("sTermIDxx")) +
                        ", cPaymForm = " + SQLUtil.toSQL("0") +
                        ", cTranStat = " + SQLUtil.toSQL(trans.getString("cTranStat")) +
                        ", sClientID = " + SQLUtil.toSQL(trans.getString("sClientID")) +
                        ", nDiscount = " + SQLUtil.toSQL(0.00) +
                        ", nAddDiscx = " + SQLUtil.toSQL(0.00) +
                        ", sSalesMan = " + SQLUtil.toSQL(trans.getString("sSalesman")) +
                        ", sModified = " + SQLUtil.toSQL(trans.getString("sModified")) +
                        ", dModified = " + SQLUtil.toSQL(trans.getString("dModified"));
                
                System.out.println(lsSQL);
                if (posDB.executeUpdate(lsSQL) <= 0){
                    posDB.rollbackTrans();
                    System.err.println("transferSales().Unable to execute query: " + lsSQL);
                    return false;
                }
                
                //load detail record
                lsSQL = "SELECT" + 
                            "  sTransNox" +
                            ", nEntryNox" +
                            ", sStockIDx" +
                            ", nQuantity" +
                            ", nReturnxx" +
                            ", nUnitPrce" +
                            ", nDiscRate" +
                            ", nDiscAmtx" +
                            ", sSerialID" +
                            ", dModified" +
                        " FROM CP_SO_Detail" + 
                        " WHERE sTransNox = " + SQLUtil.toSQL(trans.getString("sTransNox"));
                
                ResultSet loDetail = maindb.executeQuery(lsSQL);
                
                int lnCtr = 1;
                while (loDetail.next()){
                    lnItemTotal += (loDetail.getInt("nQuantity")  * 
                                    (loDetail.getDouble("nUnitPrce") - (loDetail.getDouble("nUnitPrce") * loDetail.getDouble("nDiscRate") / 100))) - 
                                    loDetail.getDouble("nDiscAmtx");
                    
                    //transfer detail record
                    lsSQL = "INSERT INTO Sales_Detail SET" +
                            "  sTransNox = " + SQLUtil.toSQL(lsSalesTrans) +
                            ", nEntryNox = " + SQLUtil.toSQL(lnCtr) +	
                            ", sOrderNox = " + SQLUtil.toSQL("") +
                            ", sStockIDx = " + SQLUtil.toSQL(loDetail.getString("sStockIDx")) +
                            ", nQuantity = " + SQLUtil.toSQL(loDetail.getInt("nQuantity")) +
                            ", nInvCostx = " + SQLUtil.toSQL(loDetail.getDouble("nUnitPrce")) +
                            ", nUnitPrce = " + SQLUtil.toSQL(loDetail.getDouble("nUnitPrce")) +
                            ", nDiscount = " + SQLUtil.toSQL(loDetail.getDouble("nDiscRate")) +
                            ", nAddDiscx = " + SQLUtil.toSQL(loDetail.getDouble("nDiscAmtx")) +
                            ", sSerialID = " + SQLUtil.toSQL(loDetail.getString("sSerialID")) +
                            ", cNewStock = " + SQLUtil.toSQL("1") +
                            ", sInsTypID = " + SQLUtil.toSQL("") +
                            ", nInsAmtxx = " + SQLUtil.toSQL(0.00) +
                            ", sWarrntNo = " + SQLUtil.toSQL("") +
                            ", cUnitForm = " + SQLUtil.toSQL("1") +
                            ", sNotesxxx = " + SQLUtil.toSQL("") +
                            ", cDetailxx = " + SQLUtil.toSQL("0") +
                            ", cPromoItm = " + SQLUtil.toSQL("0") +
                            ", cComboItm = " + SQLUtil.toSQL("0") +
                            ", dModified = " + SQLUtil.toSQL(loDetail.getString("dModified"));
                    
                    System.out.println(lsSQL);
                    if (posDB.executeUpdate(lsSQL) <= 0){
                        posDB.rollbackTrans();
                        System.err.println("transferSales().Unable to execute query: " + lsSQL);
                        return false;
                    }
                    
                    lnCtr++;
                }
                
                //String lsReceiptTrans = MiscUtil.getNextCode(lsRe"Receipt_Master", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                String lsReceiptTrans = lsSalesTrans;
                String lsSalesPayment = "";
                
                String lsCreditCard = "";
                double lnCreditCard = 0.00;
                
                String lsCheck = "";
                double lnCheck = 0.00;
                
                String lsOther = "";
                double lnOther = 0.00;
                
                double lnTranTotl = 0.00;
                double lnDiscount = 0.00;
                                
                //check if the transaction has credit card payment
                lsSQL = "SELECT" +
                            "  a.sTransNox" +
                            ", b.sBankName sTerminal" +
                            ", c.sBankName" +
                            ", d.sCardname" +
                            ", a.sCrCardNo" +
                            ", a.sApprovNo" +
                            ", a.nTranTotl" +
                            ", a.nTranTotl xApprTotl" +
                            ", e.sTermName" +
                            ", a.sTermnlID" +
                            ", a.sBankIDxx" +
                            ", a.sCrCardID" +
                            ", a.sTermIDxx" +
                            ", a.sCollectd" +
                            ", a.dCollectd" +
                            ", a.dModified" +
                            ", a.sTransNox xTransNox" +
                            ", a.sCrCardNo xCrCardNo" +
                            ", a.sApprovNo xApprovNo" +
                            ", f.nAmountxx" +
                            ", f.nBaseAmtx" +
                            ", a.sBatchNox" +
                            ", f.sSourceCd" +
                            ", a.sTermIDxx" +
                        " FROM MP_Credit_Card_Transaction a" +
                                " LEFT JOIN Banks b ON a.sTermnlID = b.sBankIDxx" +
                                " LEFT JOIN Banks c ON a.sBankIDxx = c.sBankIDxx" +
                                " LEFT JOIN Card d ON a.sCrCardID = d.sCardIDxx" +
                                " LEFT JOIN Term e ON a.sTermIDxx = e.sTermIDxx" +
                            ", MP_SO_Credit_Card f" +
                        " WHERE a.sTransNox = f.sReferNox" +
                            " AND f.sSourceCd = 'CPSl'" +
                            " AND f.sTransNox = " + SQLUtil.toSQL(trans.getString("sTransNox"));
                
                ResultSet loCard = maindb.executeQuery(lsSQL);
                
                //insert credit card details
                while (loCard.next()){
                    lsCreditCard = MiscUtil.getNextCode("Credit_Card_Trans", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                    lnCreditCard += loCard.getDouble("nTranTotl");
                    
                    if (loCard.getDouble("nTranTotl") > 0.00){
                        lsSalesPayment = MiscUtil.getNextCode("Sales_Payment", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                        
                        lsSQL = " INSERT INTO Sales_Payment SET" +
                                "  sTransNox = " + SQLUtil.toSQL(lsSalesPayment) +
                                ", cPaymForm = '2'" +
                                ", nAmountxx = " + SQLUtil.toSQL(loCard.getDouble("nTranTotl")) +
                                ", sSourceCd = 'ORec'" +
                                ", sSourceNo = " + SQLUtil.toSQL(lsReceiptTrans);
                        
                        if (posDB.executeUpdate(lsSQL) <= 0){
                            posDB.rollbackTrans();
                            System.err.println("transferSales().Unable to execute query: " + lsSQL);
                            return false;
                        }
                        
                        lsSQL = "INSERT INTO Credit_Card_Trans SET" +
                            "  sTransNox = " + SQLUtil.toSQL(lsCreditCard) +
                            ", sBranchCd = " + SQLUtil.toSQL(posDB.getBranchCode()) +
                            ", sTermnlID = " + SQLUtil.toSQL(loCard.getString("sTermnlID")) +
                            ", sBankCode = " + SQLUtil.toSQL(loCard.getString("sBankIDxx")) +
                            ", sCardIDxx = " + SQLUtil.toSQL(loCard.getString("sCrCardID")) +
                            ", sCardNoxx = " + SQLUtil.toSQL(loCard.getString("sCRCardNo")) +
                            ", sApprovNo = " + SQLUtil.toSQL(loCard.getString("sApprovNo")) +
                            ", sBatchNox = " + SQLUtil.toSQL(loCard.getString("sBatchNox")) +
                            ", nAmountxx = " + SQLUtil.toSQL(loCard.getDouble("nTranTotl")) +
                            ", sTermCode = " + SQLUtil.toSQL(loCard.getString("sTermIDxx")) +
                            ", sSourceCd = " + SQLUtil.toSQL("SlPy") +
                            ", sSourceNo = " + SQLUtil.toSQL("lsSalesPayment") +
                            ", cTranStat = " + SQLUtil.toSQL("1");
                        
                        if (posDB.executeUpdate(lsSQL) <= 0){
                            posDB.rollbackTrans();
                            System.err.println("transferSales().Unable to execute query: " + lsSQL);
                            return false;
                        }
                    }
                }
                
                //check if transaction has check payment
                lsSQL = "SELECT" +
                            "  sTransNox" +
                            ", sBankIDxx" +
                            ", dCheckDte" +
                            ", sCheckNox" +
                            ", sAcctNoxx" +
                            ", nAmountxx" +
                            ", cDepositd" +
                            ", nClearing" +
                            ", cChckStat" +
                            ", cTranStat" +
                            ", dModified" +
                        " FROM CP_SO_Checks" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(trans.getString("sTransNox"));
                
                ResultSet loCheck = maindb.executeQuery(lsSQL);
                
                while (loCheck.next()){
                    lsCheck = MiscUtil.getNextCode("Check_Payment_Trans", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                    lnCheck += loCheck.getDouble("nAmountxx");
                    
                    if (loCheck.getDouble("nAmountxx") > 0.00){
                        lsSalesPayment = MiscUtil.getNextCode("Sales_Payment", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                    
                        lsSQL = " INSERT INTO Sales_Payment SET" +
                                "  sTransNox = " + SQLUtil.toSQL(lsSalesPayment) +
                                ", cPaymForm = '1'" +
                                ", nAmountxx = " + SQLUtil.toSQL(loCheck.getDouble("nAmountxx")) +
                                ", sSourceCd = 'ORec'" +
                                ", sSourceNo = " + SQLUtil.toSQL(lsReceiptTrans);

                        if (posDB.executeUpdate(lsSQL) <= 0){
                            posDB.rollbackTrans();
                            System.err.println("transferSales().Unable to execute query: " + lsSQL);
                            return false;
                        }

                        lsSQL = "INSERT INTO Check_Payment_Trans SET" + 
                                "  sTransNox = " + SQLUtil.toSQL(lsCheck) +
                                ", dTransact = " + SQLUtil.toSQL(trans.getDate("dTransact")) +
                                ", sBankCode = " + SQLUtil.toSQL(loCheck.getString("sBankIDxx")) +
                                ", sCheckNox = " + SQLUtil.toSQL(loCheck.getString("sCheckNox")) +
                                ", dCheckDte = " + SQLUtil.toSQL(loCheck.getDate("dCheckDte")) +
                                ", nAmountxx = " + SQLUtil.toSQL(loCheck.getDouble("nAmountxx")) +
                                ", sSourceCd = " + SQLUtil.toSQL("SlPy") +
                                ", sSourceNo = " + SQLUtil.toSQL(lsSalesPayment) +
                                ", cTranStat = " +  SQLUtil.toSQL("1");
                        
                        if (posDB.executeUpdate(lsSQL) <= 0){
                            posDB.rollbackTrans();
                            System.err.println("transferSales().Unable to execute query: " + lsSQL);
                            return false;
                        }
                    }
                }
                
                //check if transaction has other payment
                lsSQL = "SELECT" +
                            "  a.sTransNox" +
                            ", a.sClientID" +
                            ", a.nTotlAmnt" +
                            ", a.sRemarksx" +
                            ", a.nAmtPaidx" +
                            ", b.sCompnyNm" +
                            ", a.dModified" +
                            ", c.nTranTotl" +
                            ", a.sReferNox" +
                            ", a.sTermCode" +
                            ", e.sTermName" +
                        " FROM Other_Payment_Received a" +
                            " LEFT JOIN Term e" +
                                " ON a.sTermCode = e.sTermIDxx" +
                            ", Client_Master b" +
                            ", CP_SO_Master c" +
                            ", Payment_Processor d" +
                        " WHERE a.sClientID = b.sClientID" +
                            " AND b.sClientID = d.sClientID" +
                            " AND a.sSourceCd =  'CPSl'" +
                            " AND a.sSourceNo = c.sTransNox" +
                            " AND a.sSourceNo = " + SQLUtil.toSQL(trans.getString("sTransNox"));
                
                ResultSet loOther = maindb.executeQuery(lsSQL);
                
                while (loOther.next()){
                    lsOther = MiscUtil.getNextCode("Financer_Trans", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                    lnOther += loOther.getDouble("nTotlAmnt");
                    
                    if (loOther.getDouble("nTotlAmnt") > 0.00){
                        lsSalesPayment = MiscUtil.getNextCode("Sales_Payment", "sTransNox", true, posDB.getConnection(), posDB.getBranchCode() + "01");
                    
                        lsSQL = " INSERT INTO Sales_Payment SET" +
                                "  sTransNox = " + SQLUtil.toSQL(lsSalesPayment) +
                                ", cPaymForm = '6'" +
                                ", nAmountxx = " + SQLUtil.toSQL(loOther.getDouble("nTotlAmnt")) +
                                ", sSourceCd = 'ORec'" +
                                ", sSourceNo = " + SQLUtil.toSQL(lsReceiptTrans);

                        if (posDB.executeUpdate(lsSQL) <= 0){
                            posDB.rollbackTrans();
                            System.err.println("transferSales().Unable to execute query: " + lsSQL);
                            return false;
                        }
                        
                        //Financer trans
                        lsSQL = "INSERT INTO Financer_Trans SET" +
                                "  sTransNox = " + SQLUtil.toSQL(lsOther) +
                                ", sClientID = " + SQLUtil.toSQL(loOther.getString("sClientID")) +
                                ", sReferNox = " + SQLUtil.toSQL(loOther.getString("sReferNox")) +
                                ", nFinAmtxx = " + SQLUtil.toSQL(loOther.getDouble("nAmtPaidx")) +
                                ", nAmtPaidx = " + SQLUtil.toSQL(loOther.getDouble("nTotlAmnt")) +
                                ", sTermCode = " + SQLUtil.toSQL(loOther.getString("sTermCode")) +
                                 ", sSourceCd = " + SQLUtil.toSQL("SlPy") +
                                ", sSourceNo = " + SQLUtil.toSQL(lsSalesPayment) +
                                ", cTranStat = " +  SQLUtil.toSQL("1");

                        if (posDB.executeUpdate(lsSQL) <= 0){
                            posDB.rollbackTrans();
                            System.err.println("transferSales().Unable to execute query: " + lsSQL);
                            return false;
                        }
                        
    //                    //CP financer
    //                    lsSQL = "INSERT INTO CP_Financer SET" +
    //                            "  sFnancrID = " +
    //                            ", sCompnyNm = " +
    //                            ", sCPerson1 = " +
    //                            ", sCPPosit1 = " +
    //                            ", sTelNoxxx = " +
    //                            ", sFaxNoxxx = " +
    //                            ", sRemarksx = " +
    //                            ", sTermIDxx = " +
    //                            ", nDiscount = " +
    //                            ", nCredLimt = " +
    //                            ", nABalance = " +
    //                            ", dCltSince = " +
    //                            ", cInHousex = " +
    //                            ", cEPayment = " +
    //                            ", cRecdStat = ";
                    }
                }
                
                lnTranTotl = lnCheck + lnCreditCard + lnOther;
                lnDiscount = lnItemTotal - lnTranTotl;
                
                lsSQL = "UPDATE Sales_Master SET" +
                            "  nAmtPaidx = " + SQLUtil.toSQL(lnItemTotal) +
                            ", nTranTotl = " + SQLUtil.toSQL(lnItemTotal) +
                            ", nDiscount = " + SQLUtil.toSQL(0.00) +
                            ", nAddDiscx = " + SQLUtil.toSQL(0.00) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(lsSalesTrans);
                
                System.out.println(lsSQL);
                if (posDB.executeUpdate(lsSQL) <= 0){
                    posDB.rollbackTrans();
                    System.err.println("transferSales().Unable to execute query: " + lsSQL);
                    return false;
                }
                
                double lnVATSales = lnItemTotal / 1.12;
                double lnVATAmtxx = lnVATSales * 0.12;
                        
                //insert receipt info
                lsSQL = "INSERT INTO Receipt_Master SET" + 
                        "  sTransNox = " + SQLUtil.toSQL(lsReceiptTrans) +
                        ", sORNumber = " + SQLUtil.toSQL(MiscUtil.getNextCode("Receipt_Master", "sORNumber", false, posDB.getConnection(), "")) +
                        ", nVATSales = " + SQLUtil.toSQL(lnVATSales) +	
                        ", nVATAmtxx = " + SQLUtil.toSQL(lnVATAmtxx) +		
                        ", nNonVATSl = " + SQLUtil.toSQL(0.00) +		
                        ", nZroVATSl = " + SQLUtil.toSQL(0.00) +		
                        ", nCWTAmtxx = " + SQLUtil.toSQL(0.00) +		
                        ", nAdvPaymx = " + SQLUtil.toSQL(0.00) +		
                        ", nCashAmtx = " + SQLUtil.toSQL(lnDiscount) +		
                        ", sSourceCd = " + SQLUtil.toSQL("SL") +	
                        ", sSourceNo = " + SQLUtil.toSQL(lsSalesTrans) +	
                        ", sCashierx = " + SQLUtil.toSQL("M001111122") +	
                        ", cPrintedx = " + SQLUtil.toSQL("1") +	
                        ", cTranStat = " + SQLUtil.toSQL("1");
                
                System.out.println(lsSQL);
                if (posDB.executeUpdate(lsSQL) <= 0){
                    posDB.rollbackTrans();
                    System.err.println("transferSales().Unable to execute query: " + lsSQL);
                    return false;
                }
            }
            
            //insert daily sales
            lsSQL = "INSERT INTO Daily_Summary SET" +
                        "  sTranDate = " + SQLUtil.toSQL(SQLUtil.dateFormat(trans.getDate("dTransact"), SQLUtil.FORMAT_SHORT_DATEX)) +
                        ", sCRMNumbr = '21112317242080606'" +
                        ", sCashierx = 'M001111122'" +
                        ", nOpenBalx = 0.00" +
                        ", nCPullOut = 0.00" +
                        ", nSalesAmt = 0.00" +
                        ", nVATSales = 0.00" +
                        ", nVATAmtxx = 0.00" +
                        ", nNonVATxx = 0.00" +
                        ", nZeroRatd = 0.00" +
                        ", nDiscount = 0.00" +
                        ", nVatDiscx = 0.00" +	
                        ", nPWDDiscx = 0.00" +	
                        ", nReturnsx = 0.00" +	
                        ", nVoidAmnt = 0.00" +	
                        ", nAccuSale = 0.00" +	
                        ", nCashAmnt = 0.00" +	
                        ", nChckAmnt = 0.00" +	
                        ", nCrdtAmnt = 0.00" +	
                        ", nChrgAmnt = 0.00" +	
                        ", nGiftAmnt = '0.00'" +	
                        ", nFinAmntx = 0.00" +	
                        ", sORNoFrom = ''" +
                        ", sORNoThru = ''" +
                        ", dOpenedxx = " + SQLUtil.toSQL(trans.getDate("dTransact")) +
                        ", cTranStat = '0'";
            System.out.println(lsSQL);
            posDB.executeUpdate(lsSQL);
//            if (posDB.executeUpdate(lsSQL) <= 0){
//                posDB.rollbackTrans();
//                System.err.println("transferSales().Unable to execute query: " + lsSQL);
//                return false;
//            }
        }
        posDB.commitTrans();
        
        return true;
    }
    
    private static boolean transferClient(ResultSet trans, GRiderX maindb, GRiderX posDB) throws SQLException{        
        trans.beforeFirst();
        
        while(trans.next()){
            String lsSQL = "SELECT" + 
                                "  sClientID" +
                                ", sLastName" +
                                ", sFrstName" +
                                ", sMiddName" +
                                ", sSuffixNm" +
                                ", cGenderCd" +
                                ", cCvilStat" +
                                ", sCitizenx" +
                                ", dBirthDte" +
                                ", sBirthPlc" +
                                ", sHouseNox" +
                                ", sAddressx" +
                                ", sTownIDxx" +
                                ", sBrgyIDxx" +
                                ", sMobileNo" +
                                ", sEmailAdd" +
                                ", sCompnyNm" +
                                ", sModified" +
                                ", dModified" +
                            " FROM Client_Master" + 
                            " WHERE sClientID = " + SQLUtil.toSQL(trans.getString("sClientID"));
            
            ResultSet loRS = maindb.executeQuery(lsSQL);
            
            while(loRS.next()){
                lsSQL = "SELECT * FROM Client_Master WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID"));
                
                ResultSet loRx = posDB.executeQuery(lsSQL);
                
                //client has no record in the pos database
                if (!loRx.next()){
                    posDB.beginTrans();
                    
                    lsSQL = "INSERT INTO Client_Master SET" +
                            "  sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                            ", cClientTp = '0'" +
                            ", sLastName = " +  SQLUtil.toSQL(loRS.getString("sLastName")) +
                            ", sFrstName = " +  SQLUtil.toSQL(loRS.getString("sFrstName")) +
                            ", sMiddName = " +  SQLUtil.toSQL(loRS.getString("sMiddName")) +
                            ", sSuffixNm = " +  SQLUtil.toSQL(loRS.getString("sSuffixNm")) +
                            ", sClientNm = " + 	 SQLUtil.toSQL(loRS.getString("sCompnyNm")) +
                            ", cGenderCd = " +  SQLUtil.toSQL(loRS.getString("cGenderCd")) +
                            ", cCvilStat = " +  SQLUtil.toSQL(loRS.getString("cCvilStat")) +
                            ", sCitizenx = " +  SQLUtil.toSQL(loRS.getString("sCitizenx")) +
                            ", dBirthDte = " +  SQLUtil.toSQL(loRS.getString("dBirthDte")) +
                            ", sBirthPlc = " +  SQLUtil.toSQL(loRS.getString("sBirthPlc")) +
                            ", sAddlInfo = ''" +
                            ", sSpouseID = ''" +
                            ", cLRClient = '0'" + 
                            ", cMCClient = '0'" + 
                            ", cSCClient = '0'" + 
                            ", cSPClient = '0'" + 
                            ", cCPClient = '1'" + 
                            ", cRecdStat = '1'" + 
                            ", sModified = " + SQLUtil.toSQL(loRS.getString("sModified")) +
                            ", dModified = " + SQLUtil.toSQL(loRS.getString("dModified"));
                    
                    System.out.println(lsSQL);
                    if (posDB.executeUpdate(lsSQL) <= 0){
                        posDB.rollbackTrans();
                        System.err.println("transferClient().Unable to execute query: " + lsSQL);
                        return false;
                    }
                    
                    lsSQL = "INSERT INTO Client_Address SET" +
                            "  sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                            ", nEntryNox = 1" +
                            ", sHouseNox = " + SQLUtil.toSQL(loRS.getString("sHouseNox")) +
                            ", sAddressx = " + SQLUtil.toSQL(loRS.getString("sAddressx")) +
                            ", sBrgyIDxx = " + SQLUtil.toSQL(loRS.getString("sBrgyIDxx")) +
                            ", sTownIDxx = " + SQLUtil.toSQL(loRS.getString("sTownIDxx")) +
                            ", nPriority = 1" + 
                            ", nLatitude = 0.00" +
                            ", nLongitud = 0.00" +
                            ", cPrimaryx = '1'" +
                            ", cRecdStat = '1'" +
                            ", dModified = " + SQLUtil.toSQL(loRS.getString("dModified"));
                    
                    System.out.println(lsSQL);
                    if (posDB.executeUpdate(lsSQL) <= 0){
                        posDB.rollbackTrans();
                        System.err.println("transferClient().Unable to execute query: " + lsSQL);
                        return false;
                    }
                    
                    lsSQL = "INSERT INTO Client_Mobile SET" +
                            "  sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                            ", nEntryNox = 1" +
                            ", sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                            ", cRecdStat = '1'" +
                            ", dModified = " + SQLUtil.toSQL(loRS.getString("dModified"));
                    
                    System.out.println(lsSQL);
                    if (posDB.executeUpdate(lsSQL) <= 0){
                        posDB.rollbackTrans();
                        System.err.println("transferClient().Unable to execute query: " + lsSQL);
                        return false;
                    }
                    
                    lsSQL = "INSERT INTO Client_eMail_Address SET" +
                            "  sClientID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                            ", nEntryNox = 1" +
                            ", sEMailAdd = " + SQLUtil.toSQL(loRS.getString("sEmailAdd")) +
                            ", cRecdStat = '1'" +
                            ", dModified = " + SQLUtil.toSQL(loRS.getString("dModified"));
                    
                    System.out.println(lsSQL);
                    if (posDB.executeUpdate(lsSQL) <= 0){
                        posDB.rollbackTrans();
                        System.err.println("transferClient().Unable to execute query: " + lsSQL);
                        return false;
                    }
                    
                    posDB.commitTrans();
                }
            }
        }
        
        return true;
    }
}
