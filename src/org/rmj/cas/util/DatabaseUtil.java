package org.rmj.cas.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.rmj.appdriver.agentfx.FileUtil;
import org.rmj.replication.utility.MiscReplUtil;

public class DatabaseUtil {
    private final String sDatabase = "CASys_DBF";
    private final String sUsername = "sa";
    private final String sPassword = "Atsp,imrtptd";
    
    private String sMySQLBinx = "";
    private String sLocation1 = "";
    private String sLocation2 = "";
    private boolean bDebugMode = false;
    private boolean bInitClass = false;
    
    public DatabaseUtil(){
        if (!loadProperties()){
            System.err.println("Unable to load properties file.");
            System.exit(1);
        }
        
        sMySQLBinx = System.getProperty("system.mysql.bin");
        sLocation1 = System.getProperty("pos.database.backup.dir01");
        sLocation2 = System.getProperty("pos.database.backup.dir02");
        bDebugMode = System.getProperty("app.debug.mode").equals("1");
        
        bInitClass = true;
    }
    
    public void Backup() {
        if (!bInitClass){
            System.err.println("Class is not initialized.");
            System.exit(1);
        }
        
        Process p = null;
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");  
            LocalDateTime now = LocalDateTime.now();  
            
            Runtime runtime = Runtime.getRuntime();
            
            String lsFileName = sLocation1 + sDatabase + "(" + dtf.format(now) + ").sql";
            String lsCommand = sMySQLBinx + "mysqldump -u" + sUsername + " -p" + sPassword + " " + sDatabase + " -r " + lsFileName;            
            
            if (bDebugMode) System.out.println(lsCommand);
            
            p = runtime.exec(lsCommand);

            //change the dbpass and dbname with your dbpass and dbname
            int processComplete = p.waitFor();

            if (processComplete == 0) {
                System.out.println("Backup created successfully!");
                
                //compress the file.
                if (!MiscReplUtil.tar(lsFileName, sLocation1))
                    System.err.println("Unable to compress backup file.");
                else{
                    System.out.println("File was compressed successfully.");
                    
                    //delete the original file.
                    if (!FileUtil.fileDelete(lsFileName))
                        System.err.println("Unable to delete original file.");
                    else
                        System.out.println("Original file deleted successfully.");
                    
                    if (!sLocation2.isEmpty()){
                        //copy the backup file to the secondary directory
                        if (!FileUtil.copyFile(lsFileName + ".tar.gz", sLocation2 + sDatabase + "(" + dtf.format(now) + ").sql.tar.gz"))
                            System.err.println("Unable to copy file to the secondary directory.");
                        else
                            System.out.println("File saved on the secondary directory.");
                    }
                    
                }
                    
                System.exit(0);
            } else {
                System.err.println("Could not create the backup!");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
    
    private boolean loadProperties(){
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream("D:\\GGC_Java_Systems\\config\\rmj.properties"));
                        
            System.setProperty("app.debug.mode", po_props.getProperty("app.debug.mode"));
            System.setProperty("system.mysql.bin", po_props.getProperty("system.mysql.bin"));
            System.setProperty("pos.database.backup.dir01", po_props.getProperty("pos.database.backup.dir01"));
            System.setProperty("pos.database.backup.dir02", po_props.getProperty("pos.database.backup.dir02"));
            
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
