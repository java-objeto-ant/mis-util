package org.rmj.mis.util.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.CommonUtils;

/**
 * @author Mac 2022.07.27
 */

public class TLM_MCSO1 implements UtilityValidator{
    private final String SOURCECD = "MCSO";
    private final String START_DATE = "2018-01-01";
    private final int FILL = 20;
    private final String GLOBE = "0";
    private final String SMART = "1";
    private final String SUNPH = "2";
    
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
            sMessage = "Leads created successfully -->> \n" + 
                        "GLOBE = " + lnGlobe + "\n" + 
                        "SMART = " + lnSmart + "\n" + 
                        "SUN = " + lnSunPH + "\n";
        else
            sMessage = "No MC Sales to convert at this moment...";
            
        System.out.println(sMessage);
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
    
    private int fill_sched(String lcSubscrbr, int lnCount){
        String lsSQL = getSQ_Master(lcSubscrbr);
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        int lnRow = 0;
        String lxSubscrbr = "";
        try {
            instance.beginTrans();
            while (loRS.next()){
                lxSubscrbr = CommonUtils.classifyNetwork(loRS.getString("xMobileNo"));
                
                if (lxSubscrbr.equals(lcSubscrbr)){
                    if (isClientDuplicate(loRS.getString("sClientID"), loRS.getString("xMobileNo"))){
                        //duplicate leads
                        //update motorcycle sales table
                        lsSQL = "UPDATE MC_SO_Master SET" + 
                                    "  nLeadType = '2'" + 
                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));

                        if (instance.executeQuery(lsSQL, "MC_SO_Master", instance.getBranchCode(), "") <= 0){
                            setMessage(instance.getErrMsg() + "\n" + instance.getMessage());
                            instance.rollbackTrans();

                            System.err.println(getMessage());
                            return 0;
                        }
                    } else {
                        lsSQL = getSQ_Insert(loRS.getString("sClientID"), loRS.getString("xMobileNo"), loRS.getString("sTransNox"));
                    
                        if (!lsSQL.equals("")){
                            //insert into call outgoing
                            if (instance.executeQuery(lsSQL, "Call_Outgoing", instance.getBranchCode(), "") <= 0){
                                setMessage(instance.getErrMsg() + "\n" + instance.getMessage());
                                instance.rollbackTrans();

                                System.err.println(getMessage());
                                return 0;
                            }

                            //update motorcycle sales table
                            lsSQL = "UPDATE MC_SO_Master SET" + 
                                        "  nLeadType = '2'" + 
                                    " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));

                            if (instance.executeQuery(lsSQL, "MC_SO_Master", instance.getBranchCode(), "") <= 0){
                                setMessage(instance.getErrMsg() + "\n" + instance.getMessage());
                                instance.rollbackTrans();

                                System.err.println(getMessage());
                                return 0;
                            }

                            lnRow = lnRow + 1;

                            if (lnRow == lnCount) break;
                        }
                    }
                }
            }
            instance.commitTrans();
        } catch (SQLException ex) {
            setMessage(ex.getMessage());
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
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            if (loRS.next())
                return FILL - (loRS.getInt("xAvlLeads") > FILL ? FILL : loRS.getInt("xAvlLeads"));
        } catch (SQLException ex) {
            setMessage(ex.getMessage());
        }
        
        return FILL;
    }
    
    private String getSQ_Insert(String fsClientID,
                                String fsMobileNo,
                                String fsTransNox){
        
        String lsNetwork = CommonUtils.classifyNetwork(fsMobileNo);
        
        if (lsNetwork.equals("")) return "";
        
        return "INSERT INTO Call_Outgoing SET" +
                "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode())) +
                ", dTransact = " + SQLUtil.toSQL(instance.getServerDate())+
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
                ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
    }
    
    private String getSQ_Master(String fcSubscrbr){
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
                    ", Client_Mobile h" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = h.sClientID" +
                    " AND b.sMobileNo = h.sMobileNo" +
                    " AND h.cSubscrbr = " + SQLUtil.toSQL(fcSubscrbr) +
                    " AND a.dTransact >= " + SQLUtil.toSQL(START_DATE) + 
                    " AND a.cTranStat NOT IN ('3', '6', '7')" +
                    " AND a.cPaymForm = '0'" +
                    " AND (LENGTH(TRIM(REPLACE(REPLACE(REPLACE(b.sMobileNo,'(',''), ')',''),'-',''))) = 11" +
                        " OR LENGTH(TRIM(REPLACE(REPLACE(REPLACE(b.sPhoneNox,'(',''), ')',''),'-',''))) = 11)" +
                    " AND (a.nLeadType IS NULL OR a.nLeadType NOT IN('2','3','4', '6', '10', '18', '34', '66', '130'))" +
                " GROUP BY b.sCLientID" +
                " ORDER BY a.dTransact";
    }
    
    private boolean isClientDuplicate(String fsClientID, String fsMobileNo) throws SQLException{
        String lsSQL = "SELECT sTransNox" +
                        " FROM Call_Outgoing" +
                        " WHERE (sClientID = " + SQLUtil.toSQL(fsClientID) +
                            " OR sMobileNo = " + SQLUtil.toSQL(fsMobileNo) + ")" +
                            " AND sSourceCd = " + SQLUtil.toSQL(SOURCECD);
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        return loRS.next();
    }
}
