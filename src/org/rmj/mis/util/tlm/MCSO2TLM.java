package org.rmj.mis.util.tlm;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;

public class MCSO2TLM {
    private final String SOURCECD = "MCSO";
    private final String START_DATE = "2018-01-01";
    private final int FILL = 50;
    private final String GLOBE = "0";
    private final String SMART = "1";
    private final String SUNPH = "2";
    
    private GRider poGRider;
    private String psMessage;

    public MCSO2TLM(GRider foGRider){
        poGRider = foGRider;
    }
    
    public String getMessage(){
        return psMessage;
    }

    public boolean Create(){
        if (poGRider == null){
            psMessage = "Application driver is not set...";
            return false;
        }
        
        int lnGlobe = get2Fill(GLOBE);
        int lnSmart = get2Fill(SMART);
        int lnSunPH = get2Fill(SUNPH);
        
        //create leads for globe
        if (lnGlobe > 0) lnGlobe = fill_sched(GLOBE, lnGlobe);
        //create leads for smart
        if (lnSmart > 0) lnSmart = fill_sched(SMART, lnSmart);
        //create leads for sun
        if (lnSunPH > 0) lnSunPH = fill_sched(SUNPH, lnSunPH);
        
        if ((lnGlobe + lnSmart + lnSunPH) > 0)
            psMessage = "Leads created successfully -->> \n" + 
                        "GLOBE = " + lnGlobe + "\n" + 
                        "SMART = " + lnSmart + "\n" + 
                        "SUN = " + lnSunPH + "\n";
        else
            psMessage = "No MC Sales to convert at this moment...";
            
        System.out.println(psMessage);
        return true;
    }
    
    private int fill_sched(String lcSubscrbr, int lnCount){
        String lsSQL = getSQ_Master();
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        int lnRow = 0;
        String lxSubscrbr = "";
        try {
            poGRider.beginTrans();
            while (loRS.next()){
                lxSubscrbr = CommonUtils.classifyNetwork(loRS.getString("xMobileNo"));
                
                if (lxSubscrbr.equals(lcSubscrbr)){
                    lsSQL = getSQ_Insert(loRS.getString("sClientID"), loRS.getString("xMobileNo"), loRS.getString("sTransNox"));
                    
                    if (!lsSQL.equals("")){
                        //insert into call outgoing
                        if (poGRider.executeQuery(lsSQL, "Call_Outgoing", poGRider.getBranchCode(), "") <= 0){
                            psMessage = poGRider.getErrMsg() + "\n" + poGRider.getMessage();
                            poGRider.rollbackTrans();

                            System.err.println(psMessage);
                            return 0;
                        }
                        
                        //update motorcycle sales table
                        lsSQL = "UPDATE MC_SO_Master SET" + 
                                    "  nLeadType = '2'" + 
                                    ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) + 
                                    ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                        
                        if (poGRider.executeQuery(lsSQL, "MC_SO_Master", poGRider.getBranchCode(), "") <= 0){
                            psMessage = poGRider.getErrMsg() + "\n" + poGRider.getMessage();
                            poGRider.rollbackTrans();

                            System.err.println(psMessage);
                            return 0;
                        }
                        
                        lnRow = lnRow + 1;
                        
                        if (lnRow == lnCount) break;
                    }
                }
            }
            poGRider.commitTrans();
        } catch (SQLException ex) {
            psMessage = ex.getMessage();
            return 0;
        }

        return lnRow;
    }
    
    private int get2Fill(String lcSubscrbr){
        if (!lcSubscrbr.equals("0") &&
            !lcSubscrbr.equals("1") &&
            !lcSubscrbr.equals("2")) return 0;
        
        String lsSQL = "SELECT" +
                            "  cSubscrbr" +
                            ", COUNT(sTransNox) xAvlLeads" +
                        " FROM Call_Outgoing " +
                        " WHERE sSourceCd = 'MCSO'" +
                                " AND cTranStat IN ('1', '0')" +
                        " GROUP BY cSubscrbr";
        
        lsSQL = MiscUtil.addCondition(lsSQL, "cSubscrbr = " + SQLUtil.toSQL(lcSubscrbr));
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            if (loRS.next())
                return FILL - (loRS.getInt("xAvlLeads") > FILL ? FILL : loRS.getInt("xAvlLeads"));
        } catch (SQLException ex) {
            psMessage = ex.getMessage();
        }
        return FILL;
    }
    
    private String getSQ_Insert(String fsClientID,
                                String fsMobileNo,
                                String fsTransNox){
        
        String lsNetwork = CommonUtils.classifyNetwork(fsMobileNo);
        
        if (lsNetwork.equals("")) return "";
        
        return "INSERT INTO Call_Outgoing SET" +
                "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode())) +
                ", dTransact = " + SQLUtil.toSQL(poGRider.getServerDate())+
                ", sClientID = " + SQLUtil.toSQL(fsClientID) +
                ", sMobileNo = " + SQLUtil.toSQL(fsMobileNo) +
                ", sRemarksx = ''" +
                ", sReferNox = " + SQLUtil.toSQL(fsTransNox) +
                ", sSourceCd = " + SQLUtil.toSQL(SOURCECD)+
                ", sApprovCd = ''" +
                ", cTranStat = '0'" +
                ", sAgentIDx = ''" +
                ", dCallStrt = NULL" +
                ", dCallEndx = NULL" +
                ", nNoRetryx = 0" +
                ", cSubscrbr = " + SQLUtil.toSQL(lsNetwork) +
                ", cTLMStatx = ''" +
                ", cSMSStatx = '0'" +
                ", nSMSSentx = 0" +
                ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) +
                ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sClientID" +
                    ", CONCAT(b.sLastName, ', ', b.sFrstName) xCustName" +
                    ", CONCAT(b.sAddressx, ', ', d.sTownName, ', ', e.sProvName, ' ', d.sZippCode) xAddressx" +
                    ", b.dBirthDte" +
                    ", (YEAR(CURDATE())-YEAR(b.dBirthDte))- (RIGHT(CURDATE(),5)<RIGHT(b.dBirthDte,5)) xAgexxxxx" +
                    ", b.cGenderCd" +
                    ", f.sOccptnNm" +
                    ", b.sEmailAdd" +
                    ", c.sClientID xClientID" +
                    ", c.sClassIDx" +
                    ", a.sTransNox" +
                    ", e.sProvIDxx" +
                    ", c.cSourceCd" +
                    ", a.sTransNox" +
                    ", g.sAcctNmbr" +
                    ", TRIM(REPLACE(REPLACE(REPLACE(b.sMobileNo,'(',''), ')',''),'-','')) xMobileNo" +
                    ", TRIM(REPLACE(REPLACE(REPLACE(b.sPhoneNox,'(',''), ')',''),'-','')) xPhoneNox" +
                " FROM MC_SO_Master a" +
                        " LEFT JOIN MC_AR_Master g" +
                            " ON a.dTransact = g.dPurchase" +
                             " AND a.sClientID = g.sClientID" +
                        " LEFT JOIN TLM_Client c" +
                            " ON a.sClientID = c.sClientID" +
                    ", Client_Master b" +
                            " LEFT JOIN TownCity d" +
                                    " LEFT JOIN Province e" +
                                        " ON d.sProvIDxx = e.sProvIDxx" +
                                " ON b.sTownIDxx = d.sTownIDxx" +
                            " LEFT JOIN Occupation f" +
                                " ON b.sOccptnID = f.sOccptnID" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND a.dTransact >= " + SQLUtil.toSQL(START_DATE) + 
                    " AND a.cTranStat NOT IN ('3', '6', '7')" +
                    " AND a.cPaymForm = '0'" +
                    " AND (LENGTH(TRIM(REPLACE(REPLACE(REPLACE(b.sMobileNo,'(',''), ')',''),'-',''))) = 11" +
                        " OR LENGTH(TRIM(REPLACE(REPLACE(REPLACE(b.sPhoneNox,'(',''), ')',''),'-',''))) = 11)" +
                    " AND (a.nLeadType IS NULL OR a.nLeadType NOT IN('2','3','4', '6', '10', '18', '34', '66', '130'))" +
                " GROUP BY b.sCLientID" +
                " ORDER BY a.dTransact" +
                " LIMIT 5000";
    }
}
