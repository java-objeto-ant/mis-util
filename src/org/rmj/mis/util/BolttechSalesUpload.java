package org.rmj.mis.util;

import org.rmj.replication.utility.LogWrapper;
import org.rmj.replication.utility.SFTP_DU;

public class BolttechSalesUpload {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("BolttechSalesUpload", "mis.log");        
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        try {
            SFTP_DU sftp = new SFTP_DU();
            //sftp.setHost("13.250.94.208");
            sftp.setUser("ph_guanzon");
            sftp.setPort(22);
            sftp.setHostKey(System.getProperty("sys.default.path.config") + "/config/ph_guanzon_1.ppk");
            sftp.xConnect("13.250.94.208");
            //sftp.xUpload("D:/new-putty/boltech-key/", "/uat/sales/unprocessed", "MBG20240626.csv");
            //sftp.xUpload("D:/new-putty/boltech-key/", "/prod/sales/unprocessed", "MBG20240626.csv");
            //sftp.xDisconnect();
        } catch (Exception e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }       
    }
}
