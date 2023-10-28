package org.rmj.mis.util.others;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONArray;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.appdriver.agentfx.StringHelper;

public class UpdateMCLocation {
    public static void main(String [] args){
        final String WAREHOUSE = "M0W1";
        final String LOCATION = "026";
        
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

        String lsSQL;
        ResultSet loRS;
        ResultSet loRx;
        JSONArray loSerial = getSerial();
        
        instance.beginTrans();
        try {
            for (int lnCtr = 0; lnCtr <= loSerial.size() - 1; lnCtr++){
                //check if serial exist
                lsSQL = "SELECT" +
                            "  a.sSerialID" +
                            ", a.sBranchCd" +
                            ", IFNULL(b.sWHouseID, '') sWHouseID" +
                            ", IFNULL(b.sSectnIDx, '') sSectnIDx" +
                        " FROM MC_Serial a" +
                            " LEFT JOIN MC_Serial_Location b ON a.sSerialID = b.sSerialID" +
                        " WHERE a.sSerialID = " + SQLUtil.toSQL(loSerial.get(lnCtr));
                
                loRS = instance.executeQuery(lsSQL);
                
                if (loRS.next()){
                    if (loRS.getString("sBranchCd").equals(WAREHOUSE)){
                        if (!loRS.getString("sWHouseID").equals(LOCATION)){
                            //update serial location
                            lsSQL = "UPDATE MC_Serial_Location SET" +
                                        "  sBranchCd = " + SQLUtil.toSQL(WAREHOUSE) +
                                        ", sWHouseID = " + SQLUtil.toSQL(LOCATION) +
                                        ", sSectnIDx = ''" +
                                    " WHERE sSerialID = " + SQLUtil.toSQL(loSerial.get(lnCtr));
                            
                            if (instance.executeQuery(lsSQL, "MC_Serial_Location", instance.getBranchCode(), "") <= 0){
                                instance.rollbackTrans();
                                System.err.println("Unable to update MC Serial Location.");
                                System.exit(1);
                            }
                            
                            lsSQL = "SELECT * FROM MC_Serial_Location_Ledger" +
                                    " WHERE sSerialID = " + SQLUtil.toSQL(loSerial.get(lnCtr)) +
                                    " ORDER BY nLedgerNo DESC LIMIT 1";
                            
                            loRx = instance.executeQuery(lsSQL);
                            
                            if (loRx.next()){
                                lsSQL = StringHelper.prepad(String.valueOf(Integer.parseInt(loRx.getString("nLedgerNo")) + 1), 2, '0');
                            } else {
                                lsSQL = "01";
                            }
                            
                            //update serial ledger
                            lsSQL = "INSERT INTO MC_Serial_Location_Ledger SET" +
                                    "  sSerialID = " + SQLUtil.toSQL(loSerial.get(lnCtr)) +
                                    ", nLedgerNo = " + SQLUtil.toSQL(lsSQL) +
                                    ", sBranchCd = " + SQLUtil.toSQL(WAREHOUSE) +
                                    ", sWHouseID = " + SQLUtil.toSQL(LOCATION) +
                                    ", sSectnIDx = ''" +
                                    ", sSourceCd = 'LTFR'";
                            
                            if (instance.executeQuery(lsSQL, "MC_Serial_Location_Ledger", instance.getBranchCode(), "") <= 0){
                                instance.rollbackTrans();
                                System.err.println("Unable to update MC_Serial_Location_Ledger.");
                                System.exit(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            instance.rollbackTrans();
            e.printStackTrace();
            System.exit(1);
        }
        
        instance.commitTrans();
    }
    
    private static JSONArray getSerial(){
        JSONArray serial = new JSONArray();
        
        serial.add("M0W123005340");
        serial.add("M0W122044927");
        serial.add("M0W122039835");
        serial.add("M0W122039849");
        serial.add("M0W122039837");
        serial.add("M0W122039843");
        serial.add("M0W122039841");
        serial.add("M0W122039834");
        serial.add("M0W122039842");
        serial.add("M0W122040821");
        serial.add("M0W122044921");
        serial.add("M0W122044914");
        serial.add("M0W122044929");
        serial.add("M0W122044916");
        serial.add("M0W122044917");
        serial.add("M0W122044918");
        serial.add("M0W122044919");
        serial.add("M0W123010770");
        serial.add("M0W123008091");
        serial.add("M0W123008092");
        serial.add("M0W123008094");
        serial.add("M0W123008087");
        serial.add("M0W123008101");
        serial.add("M0W123008102");
        serial.add("M0W123008083");
        serial.add("M0W123025424");
        serial.add("M0W123025426");
        serial.add("M0W123025427");
        serial.add("M0W123025428");
        serial.add("M0W123025429");
        serial.add("M0W123025430");
        serial.add("M0W123025431");
        serial.add("M0W123025432");
        serial.add("M0W123025433");
        serial.add("M0W123025434");
        serial.add("M0W123017044");
        serial.add("M0W123017045");
        serial.add("M0W123017047");
        serial.add("M0W123017351");
        serial.add("M0W123017352");
        serial.add("M0W123017354");
        serial.add("M0W123017041");
        serial.add("M0W123017043");
        serial.add("M0W123026155");
        serial.add("M0W123025866");
        serial.add("M0W123025435");
        serial.add("M0W123025441");
        serial.add("M0W123025436");
        serial.add("M0W123025437");
        serial.add("M0W123025438");
        serial.add("M0W123025439");
        serial.add("M0W123025440");
        serial.add("M0W123025875");
        serial.add("M0W123025876");
        serial.add("M0W123025878");
        serial.add("M0W123025879");
        serial.add("M0W123025880");
        serial.add("M0W123025881");
        serial.add("M0W123025882");
        serial.add("M0W123025883");
        serial.add("M0W123025884");
        serial.add("M0W123025867");
        serial.add("M0W123025868");
        serial.add("M0W123025865");
        serial.add("M0W123025869");
        serial.add("M0W123025870");
        serial.add("M0W123025871");
        serial.add("M0W123025874");
        serial.add("M0W123020200");
        serial.add("M0W123020201");
        serial.add("M0W123020203");
        serial.add("M0W123020208");
        serial.add("M0W123020209");
        serial.add("M0W123020210");
        serial.add("M0W123020212");
        serial.add("M0W123025890");
        serial.add("M0W123025914");
        serial.add("M0W123025891");
        serial.add("M0W123025892");
        serial.add("M0W123025915");
        serial.add("M0W123025916");
        serial.add("M0W123025893");
        serial.add("M0W123025894");
        serial.add("M0W123025917");
        serial.add("M0W123025918");
        serial.add("M0W123025895");
        serial.add("M0W123025896");
        serial.add("M0W123025897");
        serial.add("M0W123025898");
        serial.add("M0W123025909");
        serial.add("M0W123025910");
        serial.add("M0W123025911");
        serial.add("M0W123025912");
        serial.add("M0W123025889");
        serial.add("M0W123025913");
        serial.add("M0W123025886");
        serial.add("M0W123025888");
        serial.add("M0W123025905");
        serial.add("M0W123025906");
        serial.add("M0W123025907");
        serial.add("M0W123025908");
        serial.add("M0W123025899");
        serial.add("M0W123025900");
        serial.add("M0W123025901");
        serial.add("M0W123025902");
        serial.add("M0W123025903");
        serial.add("M0W123025904");
        serial.add("M0W123025414");
        serial.add("M0W123025417");
        serial.add("M0W123025418");
        serial.add("M0W123025415");
        serial.add("M0W123025413");
        serial.add("M0W123025421");
        serial.add("M0W123025422");
        serial.add("M0W123025397");
        serial.add("M0W123025398");
        serial.add("M0W123025399");
        serial.add("M0W123025400");
        serial.add("M0W123025401");
        serial.add("M0W123025402");
        serial.add("M0W123025404");
        serial.add("M0W123025405");
        serial.add("M0W123025406");
        serial.add("M0W123025403");
        serial.add("M0W123025419");
        serial.add("M0W122037360");
        serial.add("M0W122037361");
        serial.add("M0W122037362");
        serial.add("M0W123001290");
        serial.add("M0W123001291");
        serial.add("M0W123018960");
        serial.add("M0W123018961");
        serial.add("M0W123018962");
        serial.add("M0W123018963");
        serial.add("M0W123018968");
        serial.add("M0W123018958");
        serial.add("M0W123018959");
        serial.add("M0W123018969");
        serial.add("M0W123018971");
        serial.add("M0W123018973");
        serial.add("M0W123018974");
        serial.add("M0W123018975");
        serial.add("M0W123018977");
        serial.add("M0W123018967");
        serial.add("M0W123018922");
        serial.add("M0W123018926");
        serial.add("M0W123020187");
        serial.add("M0W123020188");
        serial.add("M0W123020189");
        serial.add("M0W123020190");
        serial.add("M0W123020601");
        serial.add("M0W123020628");
        serial.add("M0W123020617");
        serial.add("M0W123021110");
        serial.add("M0W123021086");
        serial.add("M0W123021083");
        serial.add("M0W123021084");
        serial.add("M0W123021085");
        serial.add("M0W123021087");
        serial.add("M0W123021098");
        serial.add("M0W123021096");
        serial.add("M0W123021117");
        serial.add("M0W123021119");
        serial.add("M0W123021120");
        serial.add("M0W123021101");
        serial.add("M0W123021122");
        serial.add("M0W123021123");
        serial.add("M0W123021105");
        serial.add("M0W123021107");
        serial.add("M0W123021109");
        serial.add("M0W123021125");
        serial.add("M0W123021130");
        serial.add("M0W123023373");
        serial.add("M0W123023362");
        serial.add("M0W123023366");
        serial.add("M0W123023354");
        serial.add("M0W123023355");
        serial.add("M0W123023370");
        serial.add("M0W123023352");
        serial.add("M0W123023356");
        serial.add("M0W123023357");
        serial.add("M0W123023353");
        serial.add("M0W123023359");
        serial.add("M0W123025387");
        serial.add("M0W123025388");
        serial.add("M0W123025389");
        serial.add("M0W123025390");
        serial.add("M0W123025359");
        serial.add("M0W123025391");
        serial.add("M0W123025392");
        serial.add("M0W123025393");
        serial.add("M0W123025394");
        serial.add("M0W123025381");
        serial.add("M0W123025361");
        serial.add("M0W123025395");
        serial.add("M0W123025396");
        serial.add("M0W123025357");
        serial.add("M0W123025358");
        serial.add("M0W123025362");
        serial.add("M0W123025371");
        serial.add("M0W123025363");
        serial.add("M0W123025364");
        serial.add("M0W123025365");
        serial.add("M0W123025366");
        serial.add("M0W123025377");
        serial.add("M0W123025378");
        serial.add("M0W123025379");
        serial.add("M0W123025380");
        serial.add("M0W123025383");
        serial.add("M0W123025384");
        serial.add("M0W123025385");
        serial.add("M0W123025386");
        serial.add("M0W123025367");
        serial.add("M0W123025368");
        serial.add("M0W123025372");
        serial.add("M0W123025369");
        serial.add("M0W123025370");
        serial.add("M0W123025373");
        serial.add("M0W123025374");
        serial.add("M0W123025375");
        serial.add("M0W123025376");
        serial.add("M0W123025347");
        serial.add("M0W123025348");
        serial.add("M0W123025350");
        serial.add("M0W123025351");
        serial.add("M0W123025352");
        serial.add("M0W123025353");
        serial.add("M0W123025354");
        serial.add("M0W123025355");
        serial.add("M0W123025356");
        serial.add("M0W123025110");
        serial.add("M0W123025157");
        serial.add("M0W123025160");
        serial.add("M0W123025101");
        serial.add("M0W123025103");
        serial.add("M0W123025104");
        serial.add("M0W123025105");
        serial.add("M0W123025106");
        serial.add("M0W123025107");
        serial.add("M0W123025108");
        serial.add("M0W123025109");
        serial.add("M0W123025175");
        serial.add("M0W123025134");
        serial.add("M0W123025139");
        serial.add("M0W123026495");
        serial.add("M0W123025185");
        serial.add("M0W123025189");
        serial.add("M0W123025190");
        serial.add("M0W123025131");
        serial.add("M0W123026496");
        serial.add("M0W123026497");
        serial.add("M0W123026516");
        serial.add("M0W123026518");
        serial.add("M0W123026504");
        serial.add("M0W123026522");
        serial.add("M0W123026523");
        serial.add("M0W123026524");
        serial.add("M0W123026505");
        serial.add("M0W123026506");
        serial.add("M0W123026507");
        serial.add("M0W123026535");
        serial.add("M0W123026537");
        serial.add("M0W123026539");
        serial.add("M0W123026540");
        serial.add("M0W123026541");
        serial.add("M0W123026542");
        serial.add("M0W123026543");
        serial.add("M0W123026544");
        serial.add("M0W123026515");
        serial.add("M0W123026508");
        serial.add("M0W123026499");
        serial.add("M0W123026509");
        serial.add("M0W123026510");
        serial.add("M0W123026511");
        serial.add("M0W123026512");
        serial.add("M0W123026513");
        serial.add("M0W123026514");
        serial.add("M0W123026527");
        serial.add("M0W123026525");
        serial.add("M0W123026500");
        serial.add("M0W123026529");
        serial.add("M0W123026530");
        serial.add("M0W123026531");
        serial.add("M0W123026532");
        serial.add("M0W123026501");
        serial.add("M0W123026533");
        serial.add("M0W123026502");
        serial.add("M0W123026503");
        serial.add("M0W423000930");
        serial.add("M0W423000928");
        serial.add("M0W423001031");
        serial.add("M0W122035782");
        serial.add("M0W122043251");
        serial.add("M0W122043270");
        serial.add("M0W122043263");
        serial.add("M0W122043269");
        serial.add("M0W122043258");
        serial.add("M0W122043257");
        serial.add("M0W122043254");
        serial.add("M0W122043259");
        serial.add("M0W122043255");
        serial.add("M0W122043252");
        serial.add("M0W122043282");
        serial.add("M0W123008682");
        serial.add("M0W123008686");
        serial.add("M0W123010229");
        serial.add("M0W123010231");
        serial.add("M0W123010228");
        serial.add("M0W123010221");
        serial.add("M0W123010235");
        serial.add("M0W123010216");
        serial.add("M0W123013103");
        serial.add("M0W123013106");
        serial.add("M0W123013102");
        serial.add("M0W123021461");
        serial.add("M0W123021459");
        serial.add("M0W123021456");
        serial.add("M0W123021458");
        serial.add("M0W123021455");
        serial.add("M0W123021457");
        serial.add("M0W123021443");
        serial.add("M0W123021444");
        serial.add("M0W123021442");
        serial.add("M0W123021462");
        serial.add("M0W123021465");
        serial.add("M0W123021441");
        serial.add("M0W123021463");
        serial.add("M0W123021435");
        serial.add("M0W123021437");
        serial.add("M0W123021436");
        serial.add("M0W123021430");
        serial.add("M0W123021432");
        serial.add("M0W123021431");
        serial.add("M0W123021433");
        serial.add("M0W123021434");
        serial.add("M0W123021410");
        serial.add("M0W123021411");
        serial.add("M0W123021419");
        serial.add("M0W123021418");
        serial.add("M0W123021414");
        serial.add("M0W123025076");
        serial.add("M0W123025077");
        serial.add("M0W123025075");
        serial.add("M0W123025078");
        serial.add("M0W123025073");
        serial.add("M0W123025072");
        serial.add("M0W123025071");
        serial.add("M0W123025169");
        serial.add("M0W123025168");
        serial.add("M0W123025163");
        serial.add("M0W123025170");
        serial.add("M0W123025164");
        serial.add("M0W123025141");
        serial.add("M0W123025166");
        serial.add("M0W123025165");
        serial.add("M0W123025142");
        serial.add("M0W123025146");
        serial.add("M0W123025143");
        serial.add("M0W123025145");
        serial.add("M0W123025144");
        serial.add("M0W123025079");
        serial.add("M0W123025080");
        serial.add("M0W123025161");
        serial.add("M0W123025162");
        serial.add("M0W123025148");
        serial.add("M0W123025149");
        serial.add("M0W123025150");
        serial.add("M0W123025860");
        serial.add("M0W123025864");
        serial.add("M0W123025863");
        serial.add("M0W123025862");
        serial.add("M0W123025855");
        serial.add("M0W123025856");
        serial.add("M0W123025861");
        serial.add("M0W123025857");
        serial.add("M0W123025858");
        serial.add("M0W123025859");
        serial.add("M0W123026125");
        serial.add("M0W123026129");
        serial.add("M0W123026130");
        serial.add("M0W123026128");
        serial.add("M0W123026127");
        serial.add("M0W123026126");
        serial.add("M0W123026154");
        serial.add("M0W123026153");
        serial.add("M0W123026146");
        serial.add("M0W123026147");
        serial.add("M0W123026150");
        serial.add("M0W123026145");
        serial.add("M0W123026151");
        serial.add("M0W123026134");
        serial.add("M0W123026133");
        serial.add("M0W123026132");
        serial.add("M0W123026131");
        serial.add("M0W123026143");
        serial.add("M0W123026136");
        serial.add("M0W123026140");
        serial.add("M0W123026137");
        serial.add("M0W123026144");
        serial.add("M0W123026141");
        serial.add("M0W123026138");
        serial.add("M0W123026139");
        serial.add("M0W123000906");
        serial.add("M0W123000916");
        serial.add("M0W123000917");
        serial.add("M0W122028219");
        serial.add("M0W122028220");
        serial.add("M0W122028222");
        serial.add("M0W122028216");
        serial.add("M0W122028214");
        serial.add("M0W123002906");
        serial.add("M0W123002905");
        serial.add("M0W123002898");
        serial.add("M0W123002897");
        serial.add("M0W123002896");
        serial.add("M0W123002899");
        serial.add("M0W123002894");
        serial.add("M0W123002902");
        serial.add("M0W123002900");
        serial.add("M0W123002901");
        serial.add("M0W123002883");
        serial.add("M0W123002891");
        serial.add("M0W123002881");
        serial.add("M0W123020234");
        serial.add("M0W123020236");
        serial.add("M0W123020235");
        serial.add("M0W123020237");
        serial.add("M0W123007847");
        serial.add("M0W122032930");
        serial.add("M0W122032929");
        serial.add("M0W122032945");
        serial.add("M0W122034902");
        serial.add("M0W122034900");
        serial.add("M0W122034901");
        serial.add("M0W122034903");
        serial.add("M0W122034905");
        serial.add("M0W122034907");
        serial.add("M0W122034908");
        serial.add("M0W122034909");
        serial.add("M0W123001447");
        serial.add("M0W123001451");
        serial.add("M0W123001455");
        serial.add("M0W123001438");
        serial.add("M0W123001439");
        serial.add("M0W123001441");
        serial.add("M0W123001442");
        serial.add("M0W123025117");
        serial.add("M0W123025111");
        serial.add("M0W123025112");
        serial.add("M0W123025113");
        serial.add("M0W123025114");
        serial.add("M0W123025115");
        serial.add("M0W123025119");
        serial.add("M0W123025120");
        serial.add("M0W123025121");
        serial.add("M0W123025122");
        serial.add("M0W123025123");
        serial.add("M0W123025129");
        serial.add("M0W123025124");
        serial.add("M0W123025130");
        serial.add("M0W123025125");
        serial.add("M0W123025126");
        serial.add("M0W123025127");
        serial.add("M0W123025128");
        serial.add("M0W123025082");
        serial.add("M0W123025083");
        serial.add("M0W123025084");
        serial.add("M0W123025085");
        serial.add("M0W123025086");
        serial.add("M0W123025087");
        serial.add("M0W123025088");
        serial.add("M0W123025090");
        serial.add("M0W123025091");
        serial.add("M0W123025092");
        serial.add("M0W123025094");
        serial.add("M0W123025095");
        serial.add("M0W123025096");
        serial.add("M0W123025100");
    
        return serial;
    }
}
