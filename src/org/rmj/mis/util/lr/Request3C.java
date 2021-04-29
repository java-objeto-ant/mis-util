/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.mis.util.lr;

import java.util.ArrayList;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;

/**
 *
 * @author mac
 */
public class Request3C {
    public static void main (String [] args){
        final String PRODUCTID = "gRider";
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
        
        ArrayList loArr = getAccounts();
        String lsSQL;
        
        if (!loArr.isEmpty()){
            poGRider.beginTrans();
            int lnCtr;
            for (lnCtr = 0; lnCtr <= loArr.size()-1; lnCtr++){
                lsSQL = "INSERT INTO LR_3C_Request SET" +
                            "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("LR_3C_Request", "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode())) +
                            ", dTransact = '2021-04-29'" + 
                            ", dCollDate = '2021-04-29'" + 
                            ", sAcctNmbr = " + SQLUtil.toSQL(loArr.get(lnCtr)) + 
                            ", sMobileNo = ''" +
                            ", sRequestx = 'M00109011794'" + 
                            ", sApproved = ''" + 
                            ", sRemarksx = ''" + 
                            ", cTranStat = '0'" + 
                            ", sModified = " + SQLUtil.toSQL(poGRider.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());

                if (poGRider.executeQuery(lsSQL, "LR_3C_Request", poGRider.getBranchCode(), "") <= 0){
                    poGRider.rollbackTrans();
                    System.err.println(poGRider.getErrMsg() + "; " + poGRider.getMessage());
                    System.exit(1);
                }
            }
            poGRider.commitTrans();
            
            System.out.print("Accounts have 3C request was created successfully. " + lnCtr);
        } else 
            System.out.print("No accounts detected.");
    }
    
    private static ArrayList getAccounts(){
        ArrayList instance = new ArrayList();
        
        instance.add("M025200043");
        instance.add("M078200038");
        instance.add("M137190292");
        instance.add("M071200085");
        instance.add("M071190292");
        instance.add("M095200078");
        instance.add("M058200101");
        instance.add("M170200020");
        instance.add("M069200129");
        instance.add("M068200031");
        instance.add("M030200063");
        instance.add("M150200035");
        instance.add("M042200076");
        instance.add("M008190297");
        instance.add("M049190351");
        instance.add("M137200119");
        instance.add("M058200101");
        instance.add("M129210010");
        instance.add("M043200088");
        instance.add("M021210007");
        instance.add("M082200154");
        instance.add("M115210012");
        instance.add("M039200060");
        instance.add("M048200157");
        instance.add("M130210013");
        instance.add("M170200021");
        instance.add("M120200222");
        instance.add("M132200126");
        instance.add("M039200118");
        instance.add("M042200076");
        instance.add("M086200092");
        instance.add("M145200052");
        instance.add("M005200150");
        instance.add("M019210007");
        instance.add("M170200020");
        instance.add("M058200046");
        instance.add("M039200104");
        instance.add("M102210003");
        instance.add("M156210012");
        instance.add("M082200182");
        instance.add("M075210007");
        instance.add("M093200081");
        instance.add("M072200070");
        instance.add("M102210005");
        instance.add("M005210012");
        instance.add("M103210002");
        instance.add("M058210002");
        instance.add("M115210020");
        instance.add("M099210009");
        instance.add("M111200070");
        instance.add("M150210018");
        instance.add("M053210015");
        instance.add("M040200168");
        instance.add("M079210030");
        instance.add("M148210003");
        instance.add("M029200089");
        instance.add("M163200031");
        instance.add("M091200208");
        instance.add("M146210017");
        instance.add("M105210001");
        instance.add("M011200166");
        instance.add("M039210031");
        instance.add("M007200132");
        instance.add("M098200081");
        instance.add("M149200047");
        instance.add("M016210003");
        instance.add("M147200027");
        instance.add("M029210002");
        instance.add("M105200142");
        instance.add("M061200074");
        instance.add("M146190023");
        instance.add("M059190127");
        instance.add("M088180088");
        instance.add("M098190128");
        instance.add("M127200014");
        instance.add("M064190195");
        instance.add("M057190137");
        instance.add("M102180034");
        instance.add("M004190230");
        instance.add("M015190111");
        instance.add("M101190120");
        instance.add("M062180039");
        instance.add("M008190198");
        instance.add("M091180259");
        instance.add("M094190047");
        instance.add("M115190058");
        instance.add("M122200013");
        instance.add("M132180110");
        instance.add("M023190111");
        instance.add("M068190095");
        instance.add("M036190108");
        instance.add("M093190154");
        instance.add("M120190167");
        instance.add("M138190033");
        instance.add("M060190296");
        instance.add("M104190111");
        instance.add("M064200166");
        instance.add("M118190084");
        instance.add("M005190051");
        instance.add("M063200116");
        instance.add("M102190035");
        instance.add("M055190393");
        instance.add("M054210008");
        instance.add("M090190045");
        instance.add("M131190062");
        instance.add("M093190311");
        instance.add("M099190089");
        instance.add("M113180095");
        instance.add("M053190068");
        instance.add("M072190002");
        instance.add("M143200001");
        instance.add("M045190100");
        instance.add("M057200035");
        instance.add("M142190119");
        instance.add("M063190209");
        instance.add("M043180061");
        instance.add("M116180096");
        instance.add("M042190062");
        instance.add("M090190064");
        instance.add("M082200134");
        instance.add("M093200013");
        instance.add("M035210063");
        instance.add("M052190103");
        instance.add("M072190025");
        instance.add("M119190109");
        instance.add("M128200009");
        instance.add("M035180245");
        instance.add("M142190145");
        instance.add("M071190166");
        instance.add("M077190071");
        instance.add("M076190054");
        instance.add("M018190005");
        instance.add("M001200048");
        instance.add("M046210019");
        instance.add("M074200014");
        instance.add("M016190034");
        instance.add("M071210009");
        instance.add("M098180195");
        instance.add("M127190002");
        instance.add("M136190022");
        instance.add("M110180142");
        instance.add("M135180017");
        instance.add("M020190243");
        instance.add("M071190260");
        instance.add("M004180048");
        instance.add("M009190082");
        instance.add("M049190156");
        instance.add("M056190069");
        instance.add("M095180081");
        instance.add("M019180035");
        instance.add("M025190126");
        instance.add("M091190207");
        instance.add("M008190152");
        instance.add("M048180168");
        instance.add("M055180158");
        instance.add("M140190062");
        instance.add("M061180221");
        instance.add("M036180058");
        instance.add("M105190126");
        instance.add("M098190098");
        instance.add("M136190046");
        instance.add("M011190480");
        instance.add("M050190196");
        instance.add("M032190193");
        instance.add("M101190059");
        instance.add("M032180137");
        instance.add("M070180081");
        instance.add("M076190134");
        instance.add("M124190032");
        instance.add("M014180207");
        instance.add("M048190103");
        instance.add("M100190107");
        instance.add("M023180095");
        instance.add("M061180239");
        instance.add("M068190015");
        instance.add("M090180137");
        instance.add("M106180337");
        instance.add("M114190075");
        instance.add("M036180188");
        instance.add("M047180372");
        instance.add("M099190010");
        instance.add("M130190145");
        instance.add("M060190142");
        instance.add("M129190018");
        instance.add("M111190032");
        instance.add("M127190040");
        instance.add("M011200005");
        instance.add("M035190233");
        instance.add("M135190078");
        instance.add("M034180051");
        instance.add("M096190002");
        instance.add("M142190081");
        instance.add("M063180311");
        instance.add("M133190163");
        instance.add("M085180121");
        instance.add("M014180253");
        instance.add("M140190096");
        instance.add("M139190084");
        instance.add("M036190085");
        instance.add("M082190232");
        instance.add("M138190007");
        instance.add("M104190003");
        instance.add("M071200177");
        instance.add("M159210010");
        instance.add("M082210009");
        instance.add("M025210006");
        instance.add("M076200120");
        instance.add("M025200034");
        instance.add("M145200113");
        instance.add("M091210018");
        instance.add("M161200012");
        instance.add("M087210021");
        instance.add("M104200062");
        instance.add("M131210014");
        instance.add("M066210017");
        instance.add("M062200044");
        instance.add("M146200094");
        instance.add("M091200304");
        instance.add("M139200088");
        instance.add("M124200125");
        instance.add("M156200078");
        instance.add("M132210006");
        instance.add("M094200137");
        instance.add("M005210065");
        instance.add("M137200121");
        instance.add("M115200061");
        instance.add("M115200103");
        instance.add("M102200070");
        instance.add("M151210006");
        instance.add("M025200043");
        instance.add("M091200352");
        instance.add("M035200189");
        instance.add("M122210005");
        instance.add("M054200120");
        instance.add("M075200152");
        instance.add("M142200064");
        instance.add("M008210009");
        instance.add("M141200044");
        instance.add("M048200181");
        instance.add("M115200104");
        instance.add("M047200110");
        instance.add("M092200217");
        instance.add("M046200141");
        instance.add("M079210033");
        instance.add("M052200067");
        instance.add("M160200006");
        instance.add("M008210011");
        instance.add("M019200047");
        instance.add("M037210010");
        instance.add("M025200075");
        instance.add("M056200035");
        instance.add("M014210006");
        instance.add("M054200092");
        instance.add("M063200152");
        instance.add("M007200083");
        instance.add("M083210009");
        instance.add("M093200096");
        instance.add("M015200075");
        instance.add("M023200111");
        instance.add("M077200084");
        instance.add("M040200094");
        instance.add("M119210010");
        instance.add("M119200045");
        instance.add("M083210025");
        instance.add("M114200077");
        instance.add("M124210006");
        instance.add("M005200222");
        instance.add("M035200149");
        instance.add("M077200102");
        instance.add("M040200131");
        instance.add("M119200051");
        instance.add("M009210010");
        instance.add("M072200041");
        instance.add("M152210005");
        instance.add("M070200050");
        instance.add("M151200078");
        instance.add("M124200053");
        instance.add("M150200035");
        instance.add("M022200062");
        instance.add("M068200031");
        instance.add("M078200038");
        instance.add("M013200061");
        instance.add("M120200134");
        instance.add("M029210026");
        instance.add("M010200035");
        instance.add("M018200107");
        instance.add("M026200215");
        instance.add("M095200078");
        instance.add("M007200206");
        instance.add("M026200086");
        instance.add("M061200150");
        instance.add("M115200093");
        instance.add("M036200199");
        instance.add("M136200085");
        instance.add("M035200188");
        instance.add("M043200049");
        instance.add("M078200089");
        instance.add("M013200127");
        instance.add("M008210008"); 
        return instance;
    }

}

