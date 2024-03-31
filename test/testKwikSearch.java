import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javafx.scene.control.TableColumn;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GSearch;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.StringHelper;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.constants.SQLCondition;
import org.rmj.appdriver.constants.SQLSortType;

public class testKwikSearch {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX instance = new GRiderX("gRider");
        
        if (!instance.logUser("gRider", "M001111122")){
            System.exit(1);
        }
        
        String sql = "SELECT" + 
                            " a.sClientID," + 
                            " b.sCompnyNm xCustName," + 
                            " b.sMobileNo," + 
                            " a.dFollowUp," + 
                            " a.dTargetxx," + 
                            " a.sTransNox," + 
                            " 'MC_Product_Inquiry' sTableNme," + 
                            " a.dTransact," + 
                            " IFNULL(a.sCreatedx, '') sCreatedx," + 
                            " IFNULL(e.sCompnyNm, '') xAgentNme," + 
                            " f.sBranchNm" + 
                        " FROM" + 
                            " MC_Product_Inquiry a" + 
                                " LEFT JOIN xxxSysUser d" +  
                                    " LEFT JOIN Client_Master e ON d.sEmployNo = e.sClientID" + 
                                " ON a.sCreatedx = d.sUserIDxx" + 
                                " LEFT JOIN Branch f ON LEFT(a.sTransnox, 4) = f.sBranchCd," + 
                            " Client_Master b," + 
                            " Client_Mobile c" +  
                        " WHERE a.sClientID = b.sClientID" +  
                            " AND b.sClientID = c.sClientID" +  
                            " AND b.sMobileNo = c.sMobileNo" +  
                            " AND a.cTranStat = '0'" +  
                            " AND a.sInquiryx <> 'FB'" +  
                            " AND (" + 
                                " a.dFollowUp <= CURRENT_TIMESTAMP()" +  
                                    " OR (" + 
                                        " DATE_ADD(a.dTargetxx, INTERVAL - 2 DAY) <= CURRENT_DATE()" +  
                                        " AND a.dFollowUp IS NULL" + 
                                    " )" + 
                                " )" +  
                            " AND (a.dFollowUp >= '2023-12-01' OR a.dTargetxx >= '2023-12-01')";
        
        JSONObject payload = new JSONObject();
        
        //main sql statement
        payload.put("sql", sql);
        
        JSONArray columns = new JSONArray();
        
        JSONObject detail;
        
        detail = new JSONObject();
        detail.put("Header", "Transaction No");
        detail.put("Alias", "sTransNox");
        detail.put("Criteria", "a.sTransNox");
        detail.put("Format", "@@@@-@@-@@@@@@");
        detail.put("Display", "1");
        detail.put("Condition", SQLCondition.NONE);        
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.ASCENDING);
        columns.add(detail);
        
        detail = new JSONObject();
        detail.put("Header", "Encoding Branch");
        detail.put("Alias", "sBranchNm");
        detail.put("Criteria", "f.sBranchNm");
        detail.put("Format", "");
        detail.put("Display", "1");
        detail.put("Condition", "0");
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);

        detail = new JSONObject();
        detail.put("Header", "Customer Name");
        detail.put("Alias", "xCustName");
        detail.put("Criteria", "b.sCompnyNm");
        detail.put("Format", "");
        detail.put("Display", "1");
        detail.put("Condition", SQLCondition.LIKE);
        detail.put("Value", "A");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);
        
        detail = new JSONObject();
        detail.put("Header", "Mobile No");
        detail.put("Alias", "sMobileNo");
        detail.put("Criteria", "b.sMobileNo");
        detail.put("Format", "");
        detail.put("Display", "1");
        detail.put("Condition", "0");
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);
        
        detail = new JSONObject();
        detail.put("Header", "Follow Up Date");
        detail.put("Alias", "dFollowUp");
        detail.put("Criteria", "a.dFollowUp");
        detail.put("Format", "yyyy-MM-dd");
        detail.put("Display", "1");
        detail.put("Condition", "0");
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);
        
        detail = new JSONObject();
        detail.put("Header", "Target Date");
        detail.put("Alias", "dTargetxx");
        detail.put("Criteria", "a.dTargetxx");
        detail.put("Format", "yyyy-MM-dd");
        detail.put("Display", "1");
        detail.put("Condition", "0");
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);
        
        detail = new JSONObject();
        detail.put("Header", "Agent Name");
        detail.put("Alias", "xAgentNme");
        detail.put("Criteria", "IFNULL(e.sCompnyNm, '')");
        detail.put("Format", "");
        detail.put("Display", "1");
        detail.put("Condition", "0");
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);
        
        detail = new JSONObject();
        detail.put("Header", "Activity Date");
        detail.put("Alias", "dTransact");
        detail.put("Criteria", "a.dTransact");
        detail.put("Format", "");
        detail.put("Display", "1");
        detail.put("Condition", SQLCondition.NONE);
        detail.put("Value", "");
        detail.put("Sort", SQLSortType.NONE);
        columns.add(detail);
        
        //columns for headers, conditions and sorting
        payload.put("columns", columns);
        
        //default result limit
        payload.put("limit", "5");
        
        System.out.println(payload);
        
        //sample usage of the GSearch Object
        GSearch loSearch = new GSearch(payload);
        
        //get the column filter value
        System.out.println(loSearch.getColumnFilter(2));
        
        //move the column arrangement upwards or downwards
        loSearch.moveColumn(0, false);
        loSearch.moveColumn(2, true);
        
        payload = loSearch.getParameter();
        sql = (String) payload.get("sql");
        columns = (JSONArray) payload.get("columns");
        
        try {
            int ctr;
            
            //execute the statement
            ResultSet loRS = instance.executeQuery(sql);
            
            //convert resultset to cached rowset
            CachedRowSet loRx = RowSetProvider.newFactory().createCachedRowSet();
            loRx.populate(loRS);
            MiscUtil.close(loRS);
            
            //display the headers
            for (ctr = 0; ctr <= columns.size() - 1; ctr++){
                detail = (JSONObject) columns.get(ctr);
                sql = (String) detail.get("Header");
                System.out.print(sql + "|");
            }
            
            while (loRx.next()){
                System.out.println("");
                for (ctr = 0; ctr <= columns.size() - 1; ctr++){
                    detail = (JSONObject) columns.get(ctr);
                    sql = (String) detail.get("Alias");
                    System.out.print(loRx.getObject(MiscUtil.getColumnIndex(loRx, sql)) + "|");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
