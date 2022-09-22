package org.rmj.mis.android;

import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agentfx.Debug;

public class TestPostCollection {
    public static void main(String [] args){
        GRider instance = new GRider("Android");
        
        if (!instance.loadEnv("Android")){
            Debug.Println(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT a.sTransNox, " +
                            "a.nEntryNox, " +
                            "a.sAcctNmbr, " +
                            "a.sRemCodex, " +
                            "a.dModified, " +
                            "a.xFullName, " +
                            "a.sPRNoxxxx, " +
                            "a.nTranAmtx, " +
                            "a.nDiscount, " +
                            "a.dPromised, " +
                            "a.nOthersxx, " +
                            "a.sRemarksx, " +
                            "a.cTranType, " +
                            "a.nTranTotl, " +
                            "a.cApntUnit, " +
                            "a.sBranchCd, " +
                            "b.sTransNox AS sImageIDx, " +
                            "b.sFileCode, " +
                            "b.sSourceCD, " +
                            "b.sImageNme, " +
                            "b.sMD5Hashx, " +
                            "b.sFileLoct, " +
                            "b.nLongitud, " +
                            "b.nLatitude, " +
                            "c.sLastName, " +
                            "c.sFrstName, " +
                            "c.sMiddName, " +
                            "c.sSuffixNm, " +
                            "c.sHouseNox, " +
                            "c.sAddressx, " +
                            "c.sTownIDxx, " +
                            "c.cGenderxx, " +
                            "c.cCivlStat, " +
                            "c.dBirthDte, " +
                            "c.dBirthPlc, " +
                            "c.sLandline, " +
                            "c.sMobileNo, " +
                            "c.sEmailAdd, " +
                            "d.cReqstCDe AS saReqstCde, " +
                            "d.cAddrssTp AS saAddrsTp, " +
                            "d.sHouseNox AS saHouseNox, " +
                            "d.sAddressx AS saAddress, " +
                            "d.sTownIDxx AS saTownIDxx, " +
                            "d.sBrgyIDxx AS saBrgyIDxx, " +
                            "d.cPrimaryx AS saPrimaryx, " +
                            "d.nLatitude AS saLatitude, " +
                            "d.nLongitud AS saLongitude, " +
                            "d.sRemarksx AS saRemarksx," +
                            "e.cReqstCDe AS smReqstCde, " +
                            "e.sMobileNo AS smContactNox, " +
                            "e.cPrimaryx AS smPrimaryx, " +
                            "e.sRemarksx AS smRemarksx " +
                        "FROM LR_DCP_Collection_Detail a " +
                            "LEFT JOIN Image_Information b " +
                                "ON a.sTransNox = b.sSourceNo " +
                                    "AND a.sAcctNmbr = b.sDtlSrcNo " +
                            "LEFT JOIN Client_Update_Request c " +
                                "ON a.sTransNox = c.sSourceNo " +
                                "AND a.sAcctNmbr = c.sDtlSrcNo " +
                            "LEFT JOIN Address_Update_Request d " +
                                "ON a.sClientID = d.sClientID " +
                            "LEFT JOIN MOBILE_UPDATE_REQUEST e " +
                                "ON a.sClientID = e.sClientID " +
                        "WHERE a.cSendStat <> '1'";
    }
    
}
