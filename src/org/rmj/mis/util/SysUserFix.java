package org.rmj.mis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class SysUserFix {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("SysUserFix", "mis.log");
        logwrapr.info("Start of Process!");
        
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
            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT" +
                            "  sUserIDxx" +
                            ", IFNULL(sBranchCd, '') sBranchCd" +
                            ", sLogNamex" +
                            ", sPassword" +
                            ", sUserName" +
                            ", sEmployNo" +
                            ", nUserLevl" +
                            ", cUserType" +
                            ", sProdctID" +
                            ", cUserStat" +
                            ", nSysError" +
                            ", cLogStatx" +
                            ", cLockStat" +
                            ", cAllwLock" +
                            ", cAllwView" +
                            ", sCompName" +
                            ", sSkinCode" +
                        " FROM xxxSysUser";
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){
                System.out.println("");
                System.out.print(loRS.getString("sUserIDxx") + "»");
                System.out.print(loRS.getString("sBranchCd") + "»");
                System.out.print(instance.Decrypt(loRS.getString("sLogNamex")) + "»");
                System.out.print(instance.Decrypt(loRS.getString("sPassword")) + "»");
                System.out.print(instance.Decrypt(loRS.getString("sUserName")) + "»");
                System.out.print(loRS.getString("sEmployNo") + "»");
                System.out.print(loRS.getInt("nUserLevl") + "»");
                System.out.print(loRS.getString("cUserType") + "»");
                System.out.print(loRS.getString("sProdctID") + "»");
                System.out.print(loRS.getString("cUserStat") + "»");
                System.out.print(loRS.getInt("nSysError") + "»");
                System.out.print(loRS.getString("cLogStatx") + "»");
                System.out.print(loRS.getString("cLockStat") + "»");
                System.out.print(loRS.getString("cAllwLock") + "»");
                System.out.print(loRS.getString("cAllwView") + "»");
                System.out.print(loRS.getString("sCompName") + "»");
                System.out.print(loRS.getString("sSkinCode") + "»");
                
                
                
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logwrapr.severe(e.getMessage());
        }
        
        logwrapr.info("Thank you.");
    }
}
