package org.rmj.mis.util.raffle;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rmj.appdriver.MiscUtil;

public class SQLite {
    final static String URL = "D:/GGC_Java_Systems/database/";
    final static String DB = "raffle.db";
    
    public static void main(String [] args){
        //createNewDatabase(DB); //create database
        
        try {
            Connection loConn = MiscUtil.getConnection(URL, DB);
            MiscUtil.close(loConn);;
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(SQLite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:D:/GGC_Java_Systems/database/" + fileName;
 
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
}
