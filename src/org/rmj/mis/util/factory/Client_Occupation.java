/**
 * Michael Torres Cuison
 * 
 * Mac 2021-09-24
 *      Started creating this object.
 */

package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class Client_Occupation implements UtilityValidator{
    private GRiderX instance;
    private String sMessage;
    
    @Override
    public void setGRider(GRiderX foValue) {
        instance = foValue;
        
        if (instance == null){
            System.err.println("Application driver is not set.");
            System.exit(1);
        }
    }

    @Override
    public boolean Run() {
        String lsSQL = "SELECT" +
                            "  a.sTransNox" +
                            ", a.sReferNox" +
                            ", IFNULL(b.sCatInfox, b.sDetlInfo) sCatInfox" +
                            ", a.cTranStat" +
                        " FROM MC_Credit_Application a" +
                            " LEFT JOIN Credit_Online_Application b" +
                                " ON a.sReferNox = b.sTransNox" +
                        " HAVING IFNULL(a.sReferNox, '') <> '' AND NOT ISNULL(sCatInfox)";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            if (MiscUtil.RecordCount(loRS) > 0){
                JSONObject loJSON;
                JSONObject loJSON2;
                JSONObject loJSON3;
                JSONParser loParser = new JSONParser();
                
                while (loRS.next()){
                    loJSON = (JSONObject) loParser.parse(loRS.getString("sCatInfox"));
                    System.out.println("ACCOUNT ->>" + loRS.getString("sTransNox"));
                    
                    //check if applicant means record exists
                    lsSQL = "SELECT * FROM MC_Applicant_Means WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    ResultSet loRS2 = instance.executeQuery(lsSQL);
                    
                    if (loRS2.next()){
                        lsSQL = "";
                        
                        loJSON = (JSONObject) loJSON.get("means_info");
                        
                        //get employed information
                        loJSON2 = (JSONObject) loJSON.get("employed");
                        //get other income information
                        loJSON3 = (JSONObject) loJSON.get("other_income");
                        //salary must be greater than zero to be captured
                        if (Double.valueOf(String.valueOf(loJSON2.get("nSalaryxx"))) > 0){
                            double lnOthrIncm = 0.00;
                            String lsAddressx = (String) loJSON2.get("sWrkAddrx");
                            String lsEmployer = (String) loJSON2.get("sEmployer");
                            double nLenServc = Double.valueOf(String.valueOf(loJSON2.get("nLenServc")));
                            
                            if (lsAddressx.length() > 50) lsAddressx = lsAddressx.substring(0, 50);
                            if (lsEmployer.length() > 50) lsEmployer = lsEmployer.substring(0, 50);
                            
                            if (nLenServc > 40) nLenServc = 1;
                            
                            if (!String.valueOf(loJSON3.get("nOthrIncm")).isEmpty()){
                                lnOthrIncm = Double.valueOf(String.valueOf(loJSON3.get("nOthrIncm")));
                            }

                            lsSQL += ", sEmployer = " + SQLUtil.toSQL(lsEmployer) +
                                        ", sWrkAddrx = " + SQLUtil.toSQL(lsAddressx) +
                                        ", sWrkTownx = " + SQLUtil.toSQL((String) loJSON2.get("sWrkTownx")) +
                                        ", sWrkTelno = " + SQLUtil.toSQL((String) loJSON2.get("sWrkTelno")) +
                                        ", nLenServc = " + SQLUtil.toSQL(nLenServc) +
                                        ", sPosition = " + SQLUtil.toSQL((String) loJSON2.get("sPosition")) +
                                        ", sFunction = " + SQLUtil.toSQL((String) loJSON2.get("sFunction")) +
                                        ", nSalaryxx = " + SQLUtil.toSQL(Double.valueOf(String.valueOf(loJSON2.get("nSalaryxx")))) +
                                        ", nOthIncom = " + SQLUtil.toSQL(lnOthrIncm) +
                                        ", cEmpStatx = " + SQLUtil.toSQL((String) loJSON2.get("cEmpStatx"));
                        } else {
                            lsSQL += ", sEmployer = ''" +
                                        ", sWrkAddrx = ''" +
                                        ", sWrkTownx = ''" +
                                        ", sWrkTelno = ''" +
                                        ", nLenServc = 0" +
                                        ", sPosition = ''" +
                                        ", sFunction = ''" +
                                        ", nSalaryxx = 0.00" +
                                        ", nOthIncom = 0.00" +
                                        ", cEmpStatx = ''";
                        }
                        
                        //get self employed information
                        loJSON2 = (JSONObject) loJSON.get("self_employed");
                        if (Double.valueOf(String.valueOf(loJSON2.get("nBusIncom"))) > 0){
                            String lsBusiness = (String) loJSON2.get("sBusiness");
                            String lsBusAddrx = (String) loJSON2.get("sBusAddrx");
                            double lnBusLenxx = Double.valueOf(String.valueOf(loJSON2.get("nBusLenxx")));
                            
                            if (lsBusiness.length() > 35) 
                                lsBusiness =  lsBusiness.substring(0, 35);
                            
                            if (lsBusAddrx.length() > 45) 
                                lsBusAddrx = lsBusAddrx.substring(0, 45);
                            
                            if (lnBusLenxx > 40) lnBusLenxx = 1;
                            
                            lsSQL += ", sBusiness = " + SQLUtil.toSQL(lsBusiness) +
                                        ", sBusAddrx = " + SQLUtil.toSQL(lsBusAddrx) +
                                        ", sBusTownx = " + SQLUtil.toSQL((String) loJSON2.get("sBusTownx")) +
                                        ", sBusTelNo = ''" +
                                        ", nBusLenxx = " + SQLUtil.toSQL(lnBusLenxx) +
                                        ", nBusIncom = " + SQLUtil.toSQL(Double.valueOf(String.valueOf(loJSON2.get("nBusIncom"))));
                        } else {
                            lsSQL += ", sBusiness = ''" +
                                        ", sBusAddrx = ''" +
                                        ", sBusTownx = ''" +
                                        ", sBusTelNo = ''" +
                                        ", nBusLenxx = 0" +
                                        ", nBusIncom = 0.00";
                        }
                        
                        //get self financed information
                        loJSON2 = (JSONObject) loJSON.get("financed");
                        if (Double.valueOf(String.valueOf(loJSON2.get("nEstIncme"))) > 0){
                            double lnEstIncme = Math.round(Double.valueOf(String.valueOf(loJSON2.get("nEstIncme"))));
                            String sFinancer = (String) loJSON2.get("sFinancer");
                            
                            if (sFinancer.length() > 35) 
                                sFinancer =  sFinancer.substring(0, 35);
                            
                            if (loRS2.getString("sTransNox").equals("M15721000037")) lnEstIncme = 0.00;
                            
                            lsSQL += ", sFinancr2 = " + SQLUtil.toSQL(sFinancer) +
                                        ", sReltnCd2 = " + SQLUtil.toSQL((String) loJSON2.get("sReltnCde")) +
                                        ", sNatnCdx2 = " + SQLUtil.toSQL((String) loJSON2.get("sNatnCode")) +
                                        ", nEstIncm2 = " +  SQLUtil.toSQL(lnEstIncme);
                        } else {
                            lsSQL += ", sFinancr2 = ''" +
                                        ", sReltnCd2 = ''" +
                                        ", sNatnCdx2 = ''" +
                                        ", nEstIncm2 = 0.00";
                        }
                        
                        //get pensioner information
                        loJSON2 = (JSONObject) loJSON.get("pensioner");
                        if (Double.valueOf(String.valueOf(loJSON2.get("nPensionx"))) > 0){
                            lsSQL += ", cPenTypex = " + SQLUtil.toSQL((String) loJSON2.get("cPenTypex")) + 
                                        ", nPensionx = " + SQLUtil.toSQL(Double.valueOf(String.valueOf(loJSON2.get("nPensionx")))) +
                                        ", nRetrYear = " + SQLUtil.toSQL(Integer.valueOf(String.valueOf(loJSON2.get("nRetrYear"))));
                        } else {
                            lsSQL += ", cPenTypex = ''" +
                                        ", nPensionx = 0.00" +
                                        ", nRetrYear = NULL";
                        }
                        
                        lsSQL += ", nMonGross = nSalaryxx + nBusIncom" +
                                    ", nMonOther = nOthIncom + nPensionx";
                        
                        lsSQL = "UPDATE MC_Applicant_Means SET " + lsSQL.substring(2) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS2.getString("sTransNox"));
                        
                        
                        instance.beginTrans();
                        if (instance.executeQuery(lsSQL, "MC_Applicant_Means", instance.getBranchCode(), loRS2.getString("sTransNox").substring(0, 4)) <= 0){
                            instance.rollbackTrans();
                            System.err.println(instance.getMessage());
                            System.err.println(instance.getErrMsg());
                            System.exit(1);
                        }
                        instance.commitTrans();
                    }
                }
            }
        } catch (SQLException | ParseException ex) {
            instance.rollbackTrans();
            ex.printStackTrace();
            setMessage(ex.getMessage());
            
            return false;
        }
        
        System.out.println("Utility processing done.");        

        return true;
    }

    @Override
    public void setMessage(String fsValue) {
        sMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return sMessage;
    }
}
