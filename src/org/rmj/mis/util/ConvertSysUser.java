package org.rmj.mis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.rmj.appdriver.MySQLAESCrypt;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.replication.utility.LogWrapper;

public class ConvertSysUser {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("ConvertSysUser", "mis.log");
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
                            "  a.*" +
                            ", IFNULL(b.sUserIDxx, '') xUserIDxx" +
                        " FROM xxxSysUser a" + 
                            " LEFT JOIN xxxSysUserNew b ON a.sUserIDxx = b.sUserIDxx" +
                        " HAVING xUserIDxx = ''";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            String lsLogNamex;
            String lsPassword;
            String lsUserName;
            String lsEmployNo;
            
            instance.beginTrans();
            while (loRS.next()){
                if (loRS.getString("xUserIDxx").isEmpty()){
                    lsLogNamex = instance.Decrypt(loRS.getString("sLogNamex"));
                    lsPassword = instance.Decrypt(loRS.getString("sPassword"));
                    lsUserName = instance.Decrypt(loRS.getString("sUserName"));
                    lsEmployNo = loRS.getString("sEmployNo");
                    
                    
                    if (lsLogNamex != null && lsPassword != null && lsUserName != null && lsEmployNo.length() <= 12){
                        lsLogNamex = MySQLAESCrypt.Encrypt(lsLogNamex, "08220326");
                        lsPassword = MySQLAESCrypt.Encrypt(lsPassword, "08220326");
                        lsUserName = MySQLAESCrypt.Encrypt(lsUserName, "08220326");

                        lsSQL = "INSERT INTO xxxSysUserNew SET" +
                                "  sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx")) +
                                ", sBranchCd = " + SQLUtil.toSQL(loRS.getString("sBranchCd")) +
                                ", sLogNamex = " + SQLUtil.toSQL(lsLogNamex) +
                                ", sPassword = " + SQLUtil.toSQL(lsPassword) +
                                ", sUserName = " + SQLUtil.toSQL(lsUserName) +
                                ", sEmployNo = " + SQLUtil.toSQL(loRS.getString("sEmployNo")) +
                                ", nUserLevl = " + loRS.getInt("nUserLevl") +
                                ", cUserType = " + SQLUtil.toSQL(loRS.getString("cUserType")) +
                                ", sProdctID = " + SQLUtil.toSQL(loRS.getString("sProdctID")) +
                                ", cUserStat = " + SQLUtil.toSQL(loRS.getString("cUserStat")) +
                                ", nSysError = " + loRS.getInt("nSysError") +
                                ", cLogStatx = " + SQLUtil.toSQL(loRS.getString("cLogStatx")) +
                                ", cLockStat = " + SQLUtil.toSQL(loRS.getString("cLockStat")) +
                                ", cAllwLock = " + SQLUtil.toSQL(loRS.getString("cAllwLock")) +
                                ", cAllwView = " + SQLUtil.toSQL(loRS.getString("cAllwView")) +
                                ", sCompName = " + SQLUtil.toSQL(loRS.getString("sCompName")) +
                                ", sSkinCode = " + SQLUtil.toSQL(loRS.getString("sSkinCode")) +
                                ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());

                        if (instance.executeUpdate(lsSQL) <= 0){
                            instance.rollbackTrans();
                            System.err.println("Unable to update user.");
                            System.exit(1);
                        }
                        
//                        if (instance.executeQuery(lsSQL, "xxxSysUserNew", instance.getBranchCode(), "") <= 0){
//                            instance.rollbackTrans();
//                            System.err.println("Unable to update user.");
//                            System.exit(1);
//                        }
                    }   
                }
            }
            instance.commitTrans();
        } catch (SQLException e) {
            instance.rollbackTrans();
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        logwrapr.info("Thank you.");
    }
}
