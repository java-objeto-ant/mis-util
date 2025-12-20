package org.rmj.mis.util;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        
        logwrapr.info("Thank you.");
        System.exit(0);
    }
    
    private static boolean transferSales(ResultSet trans, GRiderX maindb, GRiderX posDB) throws SQLException{
        trans.beforeFirst();
        
        while(trans.next()){
            String lsSQL = "SELECT * FROM Client_Master WHERE sClientID = " + SQLUtil.toSQL(trans.getString("sReferNox"));
                
            ResultSet loRx = posDB.executeQuery(lsSQL);

            //client has no record in the pos database
            if (!loRx.next()){
                posDB.beginTrans();
                
                //transfer master record
                lsSQL = "INSERT INTO Sales_Master SET" +
                        "  sTransNox = " + SQLUtil.toSQL(trans.getString("sReferNox")) +
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
                    //transfer detail record
                    lsSQL = "INSERT INTO Sales_Detail SET" +
                            "  sTransNox = " + SQLUtil.toSQL(trans.getString("sReferNox")) +
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
                }
                
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
                if (loCard.next()){
                    lsSQL = "INSERT INTO Credit_Card_Trans SET" +
                            "  sTransNox = " +
                            ", sBranchCd = " +
                            ", sTermnlID = " +
                            ", sBankCode = " +
                            ", sCardIDxx = " +
                            ", sCardNoxx = " +
                            ", sApprovNo = " +
                            ", sBatchNox = " +
                            ", nAmountxx = " +
                            ", sTermCode = " +
                            ", sSourceCd = " +
                            ", sSourceNo = " +
                            ", sCollectd = " +
                            ", dCollectd = " +
                            ", nBankChrg = " +
                            ", nTaxAmtxx = " +
                            ", nAmtPaidx = " +
                            ", cTranStat = ";
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
                
                if (loCheck.next()){
                    lsSQL = "INSERT INTO Check_Payment_Trans SET" + 
                            "  sTransNox = " +
                            ", dTransact = " +
                            ", sBankCode = " +
                            ", sCheckNox = " +
                            ", dCheckDte = " +
                            ", nAmountxx = " +
                            ", sRemarksx = " +
                            ", nClearDay = " +
                            ", sSourceCd = " +
                            ", sSourceNo = " +
                            ", cTranStat = ";
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
                
                if (loOther.next()){
                    //CP financer
                    lsSQL = "INSERT INTO CP_Financer SET" +
                            "  sFnancrID = " +
                            ", sCompnyNm = " +
                            ", sCPerson1 = " +
                            ", sCPPosit1 = " +
                            ", sTelNoxxx = " +
                            ", sFaxNoxxx = " +
                            ", sRemarksx = " +
                            ", sTermIDxx = " +
                            ", nDiscount = " +
                            ", nCredLimt = " +
                            ", nABalance = " +
                            ", dCltSince = " +
                            ", cInHousex = " +
                            ", cEPayment = " +
                            ", cRecdStat = ";
                    
                    //Financer trans
                    lsSQL = "INSERT INTO Financer_Trans SET" +
                            "  sTransNox = " +
                            ", sClientID = " +
                            ", sReferNox = " +
                            ", nFinAmtxx = " +
                            ", nAmtPaidx = " +
                            ", sRemarksx = " +
                            ", sTermCode = " +
                            ", sSourceCd = " +
                            ", sSourceNo = " +
                            ", cTranStat = ";
                }
                
                //insert receipt info
                lsSQL = "INSERT INTO Receipt_Master SET" + 
                        "  sTransNox = " +
                        ", sORNumber = " +
                        ", nVATSales = " +	
                        ", nVATAmtxx = " +	
                        ", nNonVATSl = " +	
                        ", nZroVATSl = " +	
                        ", nCWTAmtxx = " +	
                        ", nAdvPaymx = " +	
                        ", nCashAmtx = " +	
                        ", sSourceCd = " +
                        ", sSourceNo = " +
                        ", sCashierx = " +
                        ", cPrintedx = " +
                        ", cTranStat = ";
                
                //insert sales payment info; payments other than cash
                lsSQL = " INSERT INTO Sales_Payment SET" +
                        "  sTransNox = " +
                        ", cPaymForm = " +
                        ", nAmountxx = " +
                        ", sSourceCd = " +
                        ", sSourceNo = ";
                
                posDB.commitTrans();
            }
        }
        
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
