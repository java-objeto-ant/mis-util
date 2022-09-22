package org.rmj.mis.util.sales;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.constants.TransactionStatus;

public class MCSalesTracker {    
    protected GRiderX p_oApp;
    
    protected String p_sSQLSource;
    protected String p_sFldActual;
    protected String p_sPeriod;
    protected String p_cDivision;
    
    private String p_sBranchTable;
    private String p_sAreaTable;
    private String p_sMessage;
    
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oArea;
        
    public void setGRider(GRiderX foApp){
        p_oApp = foApp;
        
        initVars();
        p_sPeriod = "";
    }
    
    public void setPeriod(String fsValue){
        p_sPeriod = fsValue;
    }
    
    public String getPeriod(){
        return p_sPeriod;
    }
    
    public void setDivision(String fsValue){
        p_cDivision = fsValue;
        initTableName();
    }
    
    public String getDivision(){
        return p_cDivision;
    }
    
    public String  getMessage(){
        return p_sMessage;
    }
    
    public int getItemCount() throws SQLException{
        p_oDetail.last();
        return p_oDetail.getRow();
    }
    
    private void initVars(){
        p_cDivision = "1";
        p_sAreaTable = "MC_Area_Performance";
        p_sBranchTable = "MC_Branch_Performance";
    }
    
    private void initTableName(){
        if (p_cDivision.equals("1")){
            //mc
            p_sAreaTable = "MC_Area_Performance";
            p_sBranchTable = "MC_Branch_Performance";
        } else {
            //mp
            p_sAreaTable = "MP_Area_Performance";
            p_sBranchTable = "MP_Branch_Performance";
        }
    }
    
    public boolean CreateSource() throws SQLException{
        if (p_sPeriod.isEmpty()) {
            if (MiscUtil.getDateDay(p_oApp.getServerDate()) >= 5){
                p_sPeriod = SQLUtil.dateFormat(p_oApp.getServerDate(), SQLUtil.FORMAT_SHORT_YEAR_MONTH);
            } else {
                p_sPeriod = SQLUtil.dateFormat(MiscUtil.dateAdd(p_oApp.getServerDate(), MiscUtil.getDateDay(p_oApp.getServerDate()) * -1), SQLUtil.FORMAT_SHORT_YEAR_MONTH);
            }
        }
                
        p_sSQLSource = getSQ_Source();
        p_sFldActual = getFieldActual();
        
        return Process();
    }
    
    protected boolean Process() throws SQLException{         
        ResultSet loRS_Source = p_oApp.executeQuery(p_sSQLSource);

        if (MiscUtil.RecordCount(loRS_Source) == 0) return false;

        loadDetail();   
        loadArea();
        
        String lsSQL;
        JSONObject loJSON;
        
        p_oApp.beginTrans();
        while (loRS_Source.next()){
            loJSON = MiscUtil.RSFindRow(p_oDetail, "sBranchCd", loRS_Source.getString("sBranchCd"));
            if (loJSON == null){
                //add row
                p_oDetail.last();
                p_oDetail.moveToInsertRow();

                MiscUtil.initRowSet(p_oDetail);
                p_oDetail.updateObject("sBranchCd", loRS_Source.getString("sBranchCd"));
                p_oDetail.updateObject("sAreaCode", loRS_Source.getString("sAreaCode"));
                p_oDetail.updateObject("sPeriodxx", p_sPeriod);
                p_oDetail.updateObject(p_sFldActual, loRS_Source.getString("xPerfrmnc"));

                p_oDetail.insertRow();
                p_oDetail.moveToCurrentRow();
                //end add row
                
                lsSQL = "INSERT INTO " + p_sBranchTable +
                            " SET sBranchCd = " + SQLUtil.toSQL(loRS_Source.getString("sBranchCd")) +
                                ", sPeriodxx = " + SQLUtil.toSQL(p_sPeriod) +
                                ", " + p_sFldActual + " = " + loRS_Source.getString("xPerfrmnc");
            } else {
                p_oDetail.updateObject(p_sFldActual, loRS_Source.getString("xPerfrmnc"));
                p_oDetail.updateRow();
                
                lsSQL = "UPDATE " + p_sBranchTable +
                        " SET " + p_sFldActual + " = " + loRS_Source.getString("xPerfrmnc") +
                        " WHERE sBranchCd = " + SQLUtil.toSQL(loRS_Source.getString("sBranchCd")) +
                            " AND sPeriodxx = " + SQLUtil.toSQL(p_sPeriod);
            }
            
            if (p_oApp.executeQuery(lsSQL, p_sBranchTable, "", "") <= 0){
                setMessage(p_oApp.getErrMsg() + "; " + p_oApp.getMessage());
                p_oApp.rollbackTrans();
                return false;
            }
            
            loJSON = MiscUtil.RSFindRow(p_oArea, "sAreaCode", loRS_Source.getString("sAreaCode"));
            if (loJSON == null){
                //add row
                p_oArea.last();
                p_oArea.moveToInsertRow();

                MiscUtil.initRowSet(p_oArea);
                p_oArea.updateObject("sAreaCode", loRS_Source.getString("sAreaCode"));
                p_oArea.updateObject("sPeriodxx", p_sPeriod);
                p_oArea.updateObject(p_sFldActual, loRS_Source.getString("xPerfrmnc"));

                p_oArea.insertRow();
                p_oArea.moveToCurrentRow();
                //end add row
            } else {
                p_oArea.updateObject(p_sFldActual, p_oArea.getDouble(p_sFldActual) + Double.valueOf(loRS_Source.getString("xPerfrmnc")));
                p_oArea.updateRow();
            }
        }
        
        p_oArea.beforeFirst();
        while(p_oArea.next()){
            if (!p_oArea.getString("sAreaCode").isEmpty()){
                lsSQL = "INSERT INTO " + p_sAreaTable +
                        " SET sAreaCode = " + SQLUtil.toSQL(p_oArea.getString("sAreaCode")) +
                            ", sPeriodxx = " + SQLUtil.toSQL(p_sPeriod) +
                            ", " + p_sFldActual + " = " + p_oArea.getDouble(p_sFldActual) +
                        " ON DUPLICATE KEY" +
                        " UPDATE " + p_sFldActual + " = " + p_oArea.getDouble(p_sFldActual);
                
                if (p_oApp.executeQuery(lsSQL, p_sAreaTable, "", "") <= 0){
                    setMessage(p_oApp.getErrMsg() + "; " + p_oApp.getMessage());
                    p_oApp.rollbackTrans();
                    return false;
                }
            }
        }
        
        p_oApp.commitTrans();
        
        return true;
    }
    
    private boolean loadDetail() throws SQLException{
        ResultSet loRS = p_oApp.executeQuery(getSQ_Master());
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oDetail = factory.createCachedRowSet();
        p_oDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return getItemCount() > 0;
    }
    
    private boolean loadArea() throws SQLException{
        ResultSet loRS = p_oApp.executeQuery(MiscUtil.addCondition(getSQ_Area(), "0=1"));
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oArea = factory.createCachedRowSet();
        p_oArea.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    public String getSQ_Source(){
        return "SELECT a.sBranchCd" +
                    ", a.sBranchNm" +
                    ", d.sAreaCode" +
                    ", COUNT(c.sSerialID) xPerfrmnc" +
                " FROM Branch a" +
                    " LEFT JOIN MC_SO_Master b" +
                        " ON b.sTransNox LIKE CONCAT(a.sBranchCd, '%')" +
                            " AND b.cTranStat <> " + SQLUtil.toSQL(TransactionStatus.STATE_CANCELLED) +
                            " AND DATE_FORMAT(b.dTransact, '%Y%m') = " + SQLUtil.toSQL(p_sPeriod) +
                            " AND b.cTranType <> '2'" +
                    " JOIN  MC_SO_Detail c" +
                        " ON b.sTransNox = c.sTransNox" +
                            " AND c.cMotorNew = '1'" +
                    ", Branch_Others d" +
                " WHERE a.sBranchCd = d.sBranchCd" +
                    " AND d.cDivision = " + SQLUtil.toSQL(p_cDivision) +
                " GROUP BY a.sBranchCd";
    }    
    
    private String getSQ_Master(){
        return "SELECT a.sBranchCd" +
                    ", b.sBranchNm" +
                    ", c.sAreaCode" +
                    ", a.sPeriodxx" +
                    ", a." + p_sFldActual +
                " FROM " + p_sBranchTable + " a" +
                    ", Branch b" +
                    ", Branch_Others c" +
                " WHERE a.sBranchCd = b.sBranchCd" +
                    " AND a.sBranchCd = c.sBranchCd" +
                    " AND a.sPeriodxx = " + SQLUtil.toSQL(p_sPeriod) +
                " ORDER BY a.sBranchCd";
    }
    
    private String getSQ_Area(){
        return "SELECT a.sAreaCode" +
                    ", a.sPeriodxx" +
                    ", b.sAreaDesc" +
                    ", a." + p_sFldActual +
                " FROM " + p_sAreaTable + " a" +
                    ", Branch_Area b" +
                " WHERE a.sAreaCode = b.sAreaCode" +
                    " AND a.sPeriodxx = " + SQLUtil.toSQL(p_sPeriod) +
                " ORDER BY a.sAreaCode";
    }
    
    public String getFieldActual(){
        return "nMCActual";
    }
    
    private void setMessage(String fsValue){
        p_sMessage = fsValue;
    }
}
