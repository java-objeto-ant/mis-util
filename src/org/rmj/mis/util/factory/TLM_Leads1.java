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

public class TLM_Leads1 implements UtilityValidator{
    private final int pxe2Fill = 15;
    private final int pxePriorityFill = 15;
    private final int pxeLastDateInqr = -60;
    private final String pxeInquiry = "2026-01-01"; //2021-05-01
    private final String pxeInquiry2 = "2025-07-01";
    
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
        //top priority MC Facebook Inquiries
        int lnTotal = 0;
        int lnFilled = fill_sched_from_inquiry("MC", "FB","0");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MC. - GLOBE");
        lnTotal += lnFilled;
        
        lnFilled = fill_sched_from_inquiry("MC", "FB", "1");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MC. - SMART");
        lnTotal += lnFilled;
        
        lnFilled = fill_sched_from_inquiry("MC", "FB", "2");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MC. - SUN");
        lnTotal += lnFilled;
        
        lnFilled = fill_sched_from_inquiry("MC", "FB", "3");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MC. - DITO");
        lnTotal += lnFilled;
        
        System.out.println("System inserted total of " + lnTotal + " facebook inquiries from MC. - ALL NETWORKS");
        
        //top priority MP Facebook Inquiries
        lnTotal = 0;
        lnFilled = fill_sched_from_inquiry("MP", "FB", "0");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MP. - GLOBE");
        lnTotal += lnFilled;
        
        lnFilled = fill_sched_from_inquiry("MP", "FB", "1");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MP. - SMART");
        lnTotal += lnFilled;
        
        lnFilled = fill_sched_from_inquiry("MP", "FB", "2");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MP. - SUN");
        lnTotal += lnFilled;   
        
        lnFilled = fill_sched_from_inquiry("MP", "FB", "3");
        System.out.println("System inserted " + lnFilled + " facebook inquiries from MP. - DITO");
        lnTotal += lnFilled;    
        
        System.out.println("System inserted total of " + lnTotal + " facebook inquiries from MP. - ALL NETWORKS");
        
        //get inquiry only
        int n2Fill = get2Fill(pxePriorityFill, "3");
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        System.out.println("For filling - dito:" + n2Fill);
        if(n2Fill > 0) fill_inqr(n2Fill, "3");
        
        n2Fill = get2Fill(pxePriorityFill, "0");
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        System.out.println("For filling - globe:" + n2Fill);
        if(n2Fill > 0) fill_inqr(n2Fill, "0");
         
        n2Fill = get2Fill(pxePriorityFill, "1");
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        System.out.println("For filling - smart:" + n2Fill);
        if(n2Fill > 0) fill_inqr(n2Fill, "1");

        n2Fill = get2Fill(pxePriorityFill, "2");
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        System.out.println("For filling - sun:" + n2Fill);
        if(n2Fill > 0) fill_inqr(n2Fill, "2");
        
        //other source
        n2Fill = get2Fill(pxe2Fill, "0");
        if(n2Fill > 0) fill_sched(n2Fill, "0");
         
        n2Fill = get2Fill(pxe2Fill, "1");
        if(n2Fill > 0) fill_sched(n2Fill, "1");

        n2Fill = get2Fill(pxe2Fill, "2");
        if(n2Fill > 0) fill_sched(n2Fill, "2");
        
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
    
    /**
    * get2Fill(int n2Fill, String lcSubScribe)
    * 
    * mac 2019.09.16
    *   added source codes
    *       MCCA - approved MC Credit Application
    *       MPIn - mobile phone inquiry
    * mac 2019.10.03
    *   added source codes
    *       GBF - Guanzon Byaheng Fiesta
    *       FSCU - Free Service Check Up
    *       DC - Display Caravan
    *       OTH - Other activity
    */
    private int get2Fill(int n2Fill, String cSubscrbr){
        String lsSQL;
        lsSQL = "SELECT COUNT(*) nRecCount"
                    + " FROM Call_Outgoing"
                    + " WHERE cTranStat IN ('0')" 
                        + " AND sSourceCD NOT IN ('LEND', 'MCSO', 'MPIn', 'GBF', 'FSCU', 'DC', 'OTH')";   
      
        switch (cSubscrbr) {
            case "0":
                lsSQL = lsSQL + " AND cSubscrbr = " + SQLUtil.toSQL(cSubscrbr);
                break;
            case "1":
                lsSQL = lsSQL + " AND cSubscrbr = " + SQLUtil.toSQL(cSubscrbr);      
                break;
            case "2":
                lsSQL = lsSQL + " AND cSubscrbr = " + SQLUtil.toSQL(cSubscrbr);
                break;
            case "3":
                lsSQL = lsSQL + " AND cSubscrbr = " + SQLUtil.toSQL(cSubscrbr);
                break;
            default:
                lsSQL = lsSQL + " AND 0=1";
        }
      
        try {
            ResultSet loRS = instance.executeQuery(lsSQL);
         
            loRS.next();
            return n2Fill - loRS.getInt("nRecCount");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }
    
    //mac 2021.05.22
    //  separate retreival of FB Inquiries to set as top priority on TLM Leads
    private int fill_sched_from_inquiry(String fsDivision, String fsInquryTp, String fcSubscrbr){
        String lsSQL;
        
        switch (fsDivision.toLowerCase()){
            case "mc":
                lsSQL = "MC_Product_Inquiry";
                break;
            case "mp":
                lsSQL = "MP_Product_Inquiry";
                break;
            default:
                return 0;
        }
        
        if (fsDivision.toLowerCase().equals("mc")){
            lsSQL = "SELECT" + 
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", " + SQLUtil.toSQL(lsSQL) + " sTableNme" + 
                        ", a.dTransact" +
                        ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    " FROM" + 
                        "  MC_Product_Inquiry a" +  
                        ", Client_Master b" + 
                        ", Client_Mobile c" +  
                    " WHERE a.sClientID = b.sClientID" +
                        " AND b.sClientID = c.sClientID" +
                        " AND b.sMobileNo = c.sMobileNo" +
                        " AND c.cSubscrbr = " + SQLUtil.toSQL(fcSubscrbr) +
                        " AND a.cTranStat = '0'" +  
                        " AND a.dTransact >= " + SQLUtil.toSQL(SQLUtil.dateFormat(MiscUtil.dateAdd(instance.getServerDate(), pxeLastDateInqr), SQLUtil.FORMAT_SHORT_DATE)) +
                        " AND a.sInquiryx = " + SQLUtil.toSQL(fsInquryTp) +
                        " AND ((a.dFollowUp <= CURRENT_TIMESTAMP())" +  
                            " OR (a.dTargetxx <= CURRENT_TIMESTAMP()" +  
                                " AND a.dFollowUp IS NULL)" +  
                            " OR (DATE_ADD(CURRENT_DATE(), INTERVAL - 2 DAY) >= a.dTransact" +  
                                " AND a.dTargetxx IS NULL" +  
                                " AND a.dFollowUp IS NULL))" +  
                    " ORDER BY a.dTransact DESC, a.dFollowUp DESC, a.dTargetxx DESC";
        } else {
            lsSQL = "SELECT" + 
                        "  b.sMobileNo" +
                        ", a.sClientID" +
                        ", a.dFollowUp" +
                        ", a.sTransNox" +
                        ", " + SQLUtil.toSQL(lsSQL) + " sTableNme" + 
                        ", a.dTransact" +
                        ", IFNULL(a.sCreatedx, '') sCreatedx" +
                    " FROM" +
                        "  MP_Product_Inquiry a" +
                        ", Client_Master b" +
                        ", Client_Mobile c" + 
                    " WHERE a.sClientID = b.sClientID" +
                        " AND b.sClientID = c.sClientID" +
                        " AND b.sMobileNo = c.sMobileNo" +
                        " AND c.cSubscrbr = " + SQLUtil.toSQL(fcSubscrbr) +
                        " AND a.cTranStat = '0'" +  
                        " AND a.dTransact >= " + SQLUtil.toSQL(SQLUtil.dateFormat(MiscUtil.dateAdd(instance.getServerDate(), pxeLastDateInqr), SQLUtil.FORMAT_SHORT_DATE)) +
                        " AND a.sInquiryx = " + SQLUtil.toSQL(fsInquryTp) +
                        " AND ((a.dFollowUp <= CURRENT_TIMESTAMP())" +  
                            " OR (a.dTargetxx <= CURRENT_TIMESTAMP()" +  
                                " AND a.dFollowUp IS NULL)" +  
                            " OR (DATE_ADD(CURRENT_DATE(), INTERVAL - 2 DAY) >= a.dTransact" +  
                                " AND a.dTargetxx IS NULL" +  
                                " AND a.dFollowUp IS NULL))" + 
                        " ORDER BY a.dTargetxx DESC, a.dFollowUp DESC, a.dTransact DESC";
        }
        
        return processLeads(instance.executeQuery(lsSQL), pxePriorityFill, fcSubscrbr);
    }
    
    private ResultSet get_inqr_old(String fsNetwork, String fsDateFrom, String fsDateThru){
        String lsSQL;
        ResultSet loRS;
        
        //1st priority
        //process inquiries with follow up and target date
        lsSQL = "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                " FROM" +
                    "  MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND c.cSubscrbr = " + SQLUtil.toSQL(fsNetwork) +
                    " AND a.cTranStat = '0'" + 
                    " AND a.sInquiryx <> 'FB'" + 
                    " AND (a.dFollowUp <= CURRENT_TIMESTAMP()" + 
                            " OR (a.dTargetxx <= CURRENT_TIMESTAMP()" + 
                                " AND a.dFollowUp IS NULL))" + 
                " HAVING dFollowUp BETWEEN " + SQLUtil.toSQL(fsDateFrom) + " AND " + SQLUtil.toSQL(fsDateThru) +
                    " OR dTargetxx BETWEEN " + SQLUtil.toSQL(fsDateFrom) + " AND " + SQLUtil.toSQL(fsDateThru) +
                " ORDER BY dFollowUp DESC, dTargetxx DESC";
                //" ORDER BY dFollowUp DESC, dTargetxx ASC";
        
        loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0) return loRS;
        
        //2nd priority
        //process inquiries without follow up and target date
        lsSQL = "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                " FROM" +
                    "  MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND c.cSubscrbr = " + SQLUtil.toSQL(fsNetwork) +
                    " AND a.cTranStat = '0'" + 
                    " AND a.sInquiryx <> 'FB'" + 
                    " AND ((a.sInquiryx IN ('WI', 'SR')" + 
                            " AND DATE_ADD(CURRENT_DATE(), INTERVAL - 2 DAY) >= a.dTransact" + 
                            " AND a.dTargetxx IS NULL" + 
                            " AND a.dFollowUp IS NULL)" +
                        " OR (a.sInquiryx IN ('WS', 'BF', 'DS', 'FS')" + 
                            " AND a.dTargetxx IS NULL" + 
                            " AND a.dFollowUp IS NULL))" +
                " ORDER BY dTransact ASC";
        
        //lsSQL += " HAVING dTransact BETWEEN " + SQLUtil.toSQL(fsDateFrom) + " AND " + SQLUtil.toSQL(fsDateThru);
        //lsSQL += " ORDER BY dTransact ASC";

        loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0) return loRS;
        
        return null;
    }
    
    private boolean fill_inqr(int n2Fill, String lcSubScribe){
        String lsSQL;
        ResultSet loRS;
        
        //1st priority
        //process inquiries with follow up and target date
        lsSQL = "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                " FROM" +
                    "  MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND c.cSubscrbr = " + SQLUtil.toSQL(lcSubScribe) +
                    " AND a.cTranStat = '0'" + 
                    " AND a.sInquiryx <> 'FB'" + 
                    " AND (a.dFollowUp <= CURRENT_TIMESTAMP()" + 
                            " OR (DATE_ADD(a.dTargetxx, INTERVAL - 2 DAY) <= CURRENT_DATE()" + 
                                " AND a.dFollowUp IS NULL))" + 
                " HAVING dFollowUp >= " + SQLUtil.toSQL(pxeInquiry) + " OR dTargetxx >= " + SQLUtil.toSQL(pxeInquiry) +
                " ORDER BY dFollowUp DESC, dTargetxx DESC";
        
//        " AND (a.dFollowUp <= CURRENT_TIMESTAMP()" + 
//                            " OR (a.dTargetxx <= CURRENT_TIMESTAMP()" + 
//                                " AND a.dFollowUp IS NULL))" + 
        
        System.out.println(lsSQL);
        loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0) processLeads(loRS, n2Fill, lcSubScribe);
        
        //get the updated leads to fill
        n2Fill = get2Fill(pxePriorityFill, lcSubScribe);
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        if(n2Fill <= 0) return true;

        //2nd priority
        //process inquiries without follow up and target date
        lsSQL = "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Product_Inquiry' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sCreatedx, '') sCreatedx" +
                " FROM" +
                    "  MC_Product_Inquiry a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND c.cSubscrbr = " + SQLUtil.toSQL(lcSubScribe) +
                    " AND a.cTranStat = '0'" + 
                    " AND a.sInquiryx <> 'FB'" + 
                    " AND ((a.sInquiryx IN ('WI', 'SR', 'ER')" + 
                            " AND DATE_ADD(CURRENT_DATE(), INTERVAL - 2 DAY) >= a.dTransact" + 
                            " AND a.dTargetxx IS NULL" + 
                            " AND a.dFollowUp IS NULL)" +
                        " OR (a.sInquiryx IN ('WS', 'BF', 'DS', 'FS')" + 
                            " AND a.dTargetxx IS NULL" + 
                            " AND a.dFollowUp IS NULL))";
        
        lsSQL += " HAVING dTransact >= " + SQLUtil.toSQL(pxeInquiry2);
        lsSQL += " ORDER BY dTransact ASC";

        loRS = instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0) processLeads(loRS, n2Fill, lcSubScribe);
        
        //get the updated leads to fill
        n2Fill = get2Fill(pxePriorityFill, lcSubScribe);
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        if(n2Fill <= 0) return true;     
        
        //3rd priority
        //process old inquiries
        loRS = get_inqr_old(lcSubScribe, "2025-06-01", "2025-06-30");
        if (loRS != null) processLeads(loRS, n2Fill, lcSubScribe);
        
        n2Fill = get2Fill(pxePriorityFill, lcSubScribe);
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        if(n2Fill <= 0) return true;     
        
        //4th priority
        //process old inquiries
        loRS = get_inqr_old(lcSubScribe, "2025-05-01", "2025-05-31");
        if (loRS != null) processLeads(loRS, n2Fill, lcSubScribe);
        
        n2Fill = get2Fill(pxePriorityFill, lcSubScribe);
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        if(n2Fill <= 0) return true;     
        
        //4th priority
        //process old inquiries
        loRS = get_inqr_old(lcSubScribe, "2025-04-01", "2025-04-30");
        if (loRS != null) processLeads(loRS, n2Fill, lcSubScribe);
        
        n2Fill = get2Fill(pxePriorityFill, lcSubScribe);
        if (n2Fill > pxePriorityFill) n2Fill = pxePriorityFill;
        if(n2Fill <= 0) return true;     

//        //last priority
//        //recycle the unreachable leads that is last called 24 hours or later
        lsSQL = "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.sReferNox sTransNox" +
                    ", CASE a.sSourceCD" +
                        " WHEN 'INQR' THEN 'MC_Product_Inquiry'" +
                        " ELSE 'MC_Credit_Application'" +
                        "	END sTableNme" +
                    ", a.dTransact" +
                    ", '5' sCreatedx" +
                    ", IFNULL(b.nUnreachx, 0) nUnreachx" +
                    ", a.sTransNox xTransNox" +
                " FROM Call_Outgoing a" +
                    ", Client_Mobile b" +
                " WHERE a.sClientID = b.sClientID" + 
                    " AND a.sMobileNo = b.sMobileNo" +
                    " AND a.dTransact >= '2020-01-01'" +
                    " AND IFNULL(a.dCallEndx, '1900-00-00 00:00:00') <= DATE_SUB(NOW(), INTERVAL 24 HOUR)" +
                    " AND a.sSourceCD IN ('INQR')" +
                    " AND a.cTLMStatx = 'UR'" +
                    " AND a.cTranStat = '2'" +
                    " AND b.cSubscrbr = " + SQLUtil.toSQL(lcSubScribe) +
                    " AND a.dTransact BETWEEN '2024-01-01' AND " + SQLUtil.toSQL(MiscUtil.dateAdd(instance.getServerDate(), -1)) +
                " HAVING IFNULL(nUnreachx, 0) < 3" +
                " ORDER BY dTransact DESC LIMIT " + n2Fill;

        loRS = instance.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) processLeads(loRS, n2Fill, lcSubScribe);
        
        return true;
    }
    
    
    //Retrieve records to be scheduled...
    //===================================
    private void fill_sched(int n2Fill, String lcSubScribe){        
        String lsSQL;
        //load MC_Referral: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        lsSQL = "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", a.dTargetxx" +
                    ", a.sTransNox" +
                    ", 'MC_Referral' sTableNme" +
                    ", a.dTransact" +
                    ", IFNULL(a.sAgentIDx, '') sCreatedx" +
                " FROM" +
                    "  MC_Referral a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" + 
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND c.cSubscrbr = '0'" +
                    " AND a.cTranStat = '0'" + 
                    " AND (a.dFollowUp <= CURRENT_TIMESTAMP() OR a.dFollowUp IS NULL)"; 

        //load Call_Incoming: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        lsSQL += " UNION ";
        lsSQL += "SELECT" + 
                    "  sMobileNo" +
                    ", '' sClientID" +
                    ", dFollowUp" +
                    ", NULL dTargetxx" +
                    ", sTransNox" +
                    ", 'Call_Incoming' sTableNme" +
                    ", dTransact" +
                    ", IFNULL(sAgentIDx, '') sCreatedx" +
                " FROM Call_Incoming" + 
                " WHERE cTranStat = '0'" + 
                    " AND (dFollowUp <= CURRENT_TIMESTAMP() OR dFollowUp IS NULL)" + 
                    " AND LENGTH(REPLACE(REPLACE(sMobileNo, '+', ''), '*', '')) = 11" +
                    " AND REPLACE(REPLACE(sMobileNo, '+', ''), '*', '') <> ''";

        //load SMS_Incoming: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        lsSQL += " UNION ";
        lsSQL += "SELECT" + 
                    "  sMobileNo" +
                    ", '' sClientID" +
                    ", dFollowUp" +
                    ", NULL dTargetxx" +
                    ", sTransNox" +
                    ", 'SMS_Incoming' sTableNme" +
                    ", dTransact" +
                    ", '' sCreatedx" +
                " FROM SMS_Incoming" + 
                " WHERE cTranStat = '0'" + 
                    " AND cReadxxxx = '1'" + 
                    " AND (dFollowUp <= CURRENT_TIMESTAMP() OR dFollowUp IS NULL)" +
                    " AND sMessagex NOT LIKE '%FSE%'";

        //load SMS_Incoming: 2 DAYS AGO upto 10 minutes before the scheduled followup...
        //kalyptus - 2018.04.18 10:59am
        //contact MP costumer only...
        lsSQL += " UNION ";
        lsSQL += "SELECT" + 
                    "  b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.dFollowUp" +
                    ", NULL dTargetxx" +
                    ", a.sClientID sTransNox" +
                    ", 'TLM_Client' sTableNme" +
                    ", a.dFollowUp dTransact" +
                    ", IFNULL(a.sAgentIDx, '') sCreatedx" +
                " FROM" +
                    "  TLM_Client a" +
                    ", Client_Master b" +
                    ", Client_Mobile c" + 
                " WHERE a.sClientID = b.sClientID" +
                    " AND b.sClientID = c.sClientID" +
                    " AND b.sMobileNo = c.sMobileNo" +
                    " AND c.cSubscrbr = " + SQLUtil.toSQL(lcSubScribe) +
                    " AND IFNULL(a.cTranStat, '0') = '0'" + 
                    " AND a.sClassIDx IN ('0001', '0002')" +
                    " AND a.cSourceCd = 'MP'" + 
                    " AND (a.dFollowUp <= CURRENT_TIMESTAMP() OR a.dFollowUp IS NULL)";

        lsSQL += "  ORDER BY dTargetxx DESC, dFollowUp DESC, dTransact DESC LIMIT 300";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        processLeads(loRS, n2Fill, lcSubScribe);
    }
    
    private int processLeads(ResultSet foResult, int n2Fill, String lcSubScribe){
        StringBuilder lsSQL = new StringBuilder();
        int lnCtr = 0;
        
        try {
            ResultSet loRS = foResult;
            
            String lsMobileNo;
            String lsClientID;
            String lsSourceCD;
            String lcSubscrbr;
            String lsCreatedx;

            instance.beginTrans();
            loRS.beforeFirst();
            while(loRS.next() && lnCtr < n2Fill){
                System.out.println("TABLE:" + loRS.getString("sTableNme")); 
                if(loRS.getString("sTableNme").equalsIgnoreCase("MC_Product_Inquiry")){
                    lsSourceCD = "INQR";
                    lsClientID = loRS.getString("sClientID");
                    lsMobileNo = loRS.getString("sMobileNo");
                    lcSubscrbr = "";
                    lsCreatedx = loRS.getString("sCreatedx");  
                } 
                else if(loRS.getString("sTableNme").equalsIgnoreCase("MP_Product_Inquiry")){
                    lsSourceCD = "MPIn";
                    lsClientID = loRS.getString("sClientID");
                    lsMobileNo = loRS.getString("sMobileNo");
                    lsCreatedx = loRS.getString("sCreatedx");
                }
                else if(loRS.getString("sTableNme").equalsIgnoreCase("MC_Referral")){
                    lsSourceCD = "RFRL";
                    lsClientID = loRS.getString("sClientID");
                    lsMobileNo = loRS.getString("sMobileNo");
                    lcSubscrbr = "";
                    lsCreatedx = loRS.getString("sCreatedx");
                }
                else if(loRS.getString("sTableNme").equalsIgnoreCase("Call_Incoming")){
                    lsSourceCD = "CALL";
                    lsClientID = getClient(loRS.getString("sMobileNo"), loRS.getString("sTransNox"));
                    lsMobileNo = loRS.getString("sMobileNo");
                    lcSubscrbr = "";
                    lsCreatedx = loRS.getString("sCreatedx");
                }
                else if(loRS.getString("sTableNme").equalsIgnoreCase("SMS_Incoming")){
                    lsSourceCD = "ISMS";
                    lsClientID = getClient(loRS.getString("sMobileNo"), loRS.getString("sTransNox"));
                    lsMobileNo = loRS.getString("sMobileNo");
                    lcSubscrbr = "";
                    lsCreatedx = loRS.getString("sCreatedx");
                }
                else if(loRS.getString("sTableNme").equalsIgnoreCase("TLM_Client")){
                    lsSourceCD = "TLMC";
                    lsClientID = loRS.getString("sClientID");
                    lsMobileNo = loRS.getString("sMobileNo");
                    lcSubscrbr = "";
                    lsCreatedx = loRS.getString("sCreatedx");
                }            
                else{
                    lsSourceCD = "";
                    lsClientID = "";
                    lsMobileNo = "";
                    lcSubscrbr = "";
                    lsCreatedx = "";
                }

                if(lsMobileNo.length() > 5){
                    System.out.println("Classify Moble for: " + lsClientID + "»" + lsMobileNo);
                    lcSubscrbr = CommonUtils.classifyNetwork(lsMobileNo);
    
                    if(lcSubScribe.equals(lcSubscrbr)){
                        //System.out.println("Checking if number has outgoing call!" + lsMobileNo);
                        //Load from Call_Outgoing if the retrieved number has a pending schedule
                        lsSQL = new StringBuilder();
                        lsSQL.append("SELECT *"
                                  + " FROM Call_Outgoing"
                                  + " WHERE sMobileNo = " + SQLUtil.toSQL(lsMobileNo)
                                    + " AND cTranStat IN ('0', '1')");
                        ResultSet loRSX = instance.executeQuery(lsSQL.toString());

                        //Create a schedule if number has no pending schedule
                        if(!loRSX.next()) {
                            lsSQL = new StringBuilder();
                            
                            switch (lsCreatedx){
                                case "5":
                                    //update recycled lead
                                    lsSQL.append("UPDATE Call_Outgoing SET" +
                                                    "  cTranStat = '5'" +
                                                    ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                                                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate()) +
                                                " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("xTransNox")));
                                    
                                    long count = instance.executeQuery(lsSQL.toString(), "Call_Outgoing", "", "");

                                    if(count == 0){
                                        System.err.println(instance.getMessage() + instance.getErrMsg());
                                        instance.rollbackTrans();
                                        System.exit(1);
                                    }
                                    lsSQL = new StringBuilder();
                                case "":
                                    lsSQL.append("INSERT INTO Call_Outgoing" 
                                            + " SET sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode()))
                                            + ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) 
                                            + ", sClientID = " + SQLUtil.toSQL(lsClientID)
                                            + ", sMobileNo = " + SQLUtil.toSQL(lsMobileNo)
                                            + ", sRemarksx = ''" 
                                            + ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"))
                                            + ", sSourceCD = " + SQLUtil.toSQL(lsSourceCD)
                                            + ", cTranStat = '0'" 
                                            + ", sAgentIDx = ''" 
                                            + ", nNoRetryx = 0"
                                            + ", cSubscrbr = " + SQLUtil.toSQL(lcSubscrbr)
                                            + ", cCallStat = '0'"
                                            + ", cTLMStatx = '0'"
                                            + ", cSMSStatx = '0'" 
                                            + ", nSMSSentx = 0" 
                                            + ", sModified = " + SQLUtil.toSQL(instance.getUserID())
                                            + ", dModified = " + SQLUtil.toSQL(instance.getServerDate()));
                                    
                                    break;
                                default:
                                    lsSQL.append("INSERT INTO Call_Outgoing" 
                                            + " SET sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Call_Outgoing", "sTransNox", true, instance.getConnection(), instance.getBranchCode()))
                                            + ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) 
                                            + ", sClientID = " + SQLUtil.toSQL(lsClientID)
                                            + ", sMobileNo = " + SQLUtil.toSQL(lsMobileNo)
                                            + ", sRemarksx = ''" 
                                            + ", sReferNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"))
                                            + ", sSourceCD = " + SQLUtil.toSQL(lsSourceCD)
                                            + ", cTranStat = '1'" 
                                            + ", sAgentIDx = " + SQLUtil.toSQL(lsCreatedx) 
                                            + ", nNoRetryx = 0"
                                            + ", cSubscrbr = " + SQLUtil.toSQL(lcSubscrbr)
                                            + ", cCallStat = '0'"
                                            + ", cTLMStatx = '0'"
                                            + ", cSMSStatx = '0'" 
                                            + ", nSMSSentx = 0" 
                                            + ", sModified = " + SQLUtil.toSQL(instance.getUserID())
                                            + ", dModified = " + SQLUtil.toSQL(instance.getServerDate()));
                            }

                            long count = instance.executeQuery(lsSQL.toString(), "Call_Outgoing", "", "");

                            if(count == 0){
                                System.err.println(instance.getMessage() + instance.getErrMsg());
                                instance.rollbackTrans();
                                System.exit(1);
                            }

                            lnCtr++;

                            if(loRS.getString("sTableNme").equalsIgnoreCase("TLM_Client")) {
                                //Tagged the source as scheduled
                                lsSQL = new StringBuilder();
                                lsSQL.append("UPDATE " + loRS.getString("sTableNme")
                                            + " SET cTranStat = '1'" 
                                            + " WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                                instance.executeQuery(lsSQL.toString(), loRS.getString("sTableNme"), "", "");
                            } else{
                                //Tagged the source as scheduled
                                lsSQL = new StringBuilder();
                                lsSQL.append("UPDATE " + loRS.getString("sTableNme")
                                        + " SET cTranStat = '1'" 
                                        + " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                                instance.executeQuery(lsSQL.toString(), loRS.getString("sTableNme"), "", "");
                            }
                        }
                        else{
                            System.out.println("Number has outgoing call!" + lsMobileNo);
                            if(loRS.getString("sTableNme").equalsIgnoreCase("TLM_Client")) {
                                //Tagged the source do not schedule
                                lsSQL = new StringBuilder();
                                lsSQL.append("UPDATE " + loRS.getString("sTableNme")
                                            + " SET cTranStat = '4'" 
                                            + " WHERE sClientID = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                                instance.executeQuery(lsSQL.toString(), loRS.getString("sTableNme"), "", "");
                            }
                            else{
                                //Tagged the source do not schedule
                                lsSQL = new StringBuilder();
                                lsSQL.append("UPDATE " + loRS.getString("sTableNme")
                                            + " SET cTranStat = '4'" 
                                            + " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                                instance.executeQuery(lsSQL.toString(), loRS.getString("sTableNme"), "", "");
                            }
                        } //if(!loRSX.next())
                    } else {
                        if (lcSubscrbr.isEmpty()){
                            if (loRS.getString("sTableNme").equalsIgnoreCase("Call_Incoming") ||
                                loRS.getString("sTableNme").equalsIgnoreCase("SMS_Incoming")){
                                lsSQL = new StringBuilder();
                                //tag transaction as invalid
                                lsSQL.append("UPDATE " + loRS.getString("sTableNme")
                                            + " SET cTranStat = '3'" 
                                            + " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                                instance.executeQuery(lsSQL.toString(), loRS.getString("sTableNme"), "", "");
                            }
                        }
                    }
                } //if(lsMobileNo.length() > 5)
            } //while(loRS.next() && lnCtr < n2Fill)
         
            instance.commitTrans();
         
        } catch (SQLException ex) {
            ex.printStackTrace();
            instance.rollbackTrans();
            System.exit(1);
        }
        
        return lnCtr;
    }
   
    private String getMobile(String fsClientID, String fsTransNox){
        String lsSQL;
        ResultSet loRS;
        
        System.out.println(fsClientID + "»" + fsTransNox);
        
        if(!fsClientID.equalsIgnoreCase(fsTransNox) ){
            //Get previous call Call_Outgoing;
            lsSQL = "SELECT sMobileNo" +
                    " FROM Call_Outgoing" +
                    " WHERE sClientID = " + SQLUtil.toSQL(fsClientID) +
                        " AND sTransNox <> " + SQLUtil.toSQL(fsTransNox) + 
                    " ORDER BY dTransact DESC" +
                    " LIMIT 1";
            try {
                loRS = instance.executeQuery(lsSQL.toString());
                if(loRS.next()) return loRS.getString("sMobileNo");
            } catch (SQLException ex) {
                ex.printStackTrace();
                return "";
            }
        }
      
        //Get Mobile from Client_Master
        lsSQL = "SELECT sMobileNo" +
                " FROM Client_Master" +
                " WHERE sClientID = " + SQLUtil.toSQL(fsClientID) +
                " AND cRecdStat = '1'";
        try {
            loRS = instance.executeQuery(lsSQL.toString());
            if(loRS.next())
                if(!loRS.getString("sMobileNo").equals(""))
                    return loRS.getString("sMobileNo");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }

        //Get Mobile from Client_Mobile
        lsSQL = "SELECT sMobileNo" +
                " FROM Client_Mobile" +
                " WHERE sClientID = " + SQLUtil.toSQL(fsClientID) +
                    " AND cRecdStat = '1'" + 
                " ORDER BY nPriority ASC";
        try {
            loRS = instance.executeQuery(lsSQL.toString());

            if(loRS.next()) return loRS.getString("sMobileNo");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }
      
        return "";
    }
    
    private String getClient(String fsMobileNo, String fsTransNox){
        String lsSQL;
        ResultSet loRS;
      
        //Get previous call Call_Outgoing;
        lsSQL = "SELECT sClientID" +
                " FROM Call_Outgoing" +
                " WHERE sMobileNo = " + SQLUtil.toSQL(fsMobileNo) + 
                    " AND sTransNox <> " + SQLUtil.toSQL(fsTransNox) +
                " ORDER BY dTransact DESC" + 
                " LIMIT 1";
        try {
            loRS = instance.executeQuery(lsSQL.toString());
            
            if(loRS.next()) return loRS.getString("sClientID");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }
      
        //Get Mobile from Client_Master
        lsSQL = "SELECT sClientID" +
                " FROM Client_Master" +
                " WHERE sMobileNo = " + SQLUtil.toSQL(fsMobileNo) +
                    " AND cRecdStat = '1'";
        try {
            loRS = instance.executeQuery(lsSQL.toString());

            if(loRS.last()){
                if(loRS.getRow() == 1){
                   loRS.first();
                   return loRS.getString("sClientID");
                } 
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";         
        }

        //Get Mobile from Client_Mobile
        lsSQL = "SELECT sClientID" +
                " FROM Client_Mobile" +
                " WHERE sMobileNo = " + SQLUtil.toSQL(fsMobileNo) +
                    " AND cRecdStat = '1'";
        try {
            loRS = instance.executeQuery(lsSQL.toString());

            if(loRS.last()){
                if(loRS.getRow() == 1){
                    loRS.first();
                    return loRS.getString("sClientID");
                } 
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }
      
        return "";
    }
    
    //get the last agent called the transaction
    private String getAgentIDx(String fsSourceCD, String fsReferNox){
        String lsSQL = "SELECT sAgentIDx FROM Call_Outgoing" +
                        " WHERE sSourceCd = " + SQLUtil.toSQL(fsSourceCD) +
                            " AND sReferNox = " + SQLUtil.toSQL(fsReferNox) +
                            " AND cTranStat = '2'" +
                        " ORDER BY sTransNox DESC LIMIT 1";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            if (loRS.next())
                return loRS.getString("sAgentIDx");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
