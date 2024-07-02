
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MySQLAESCrypt;


public class testMigrateAccounts {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("gRider");
        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }
        
        String lsSQL = "SELECT * FROM xxxSysUser WHERE `sUserIDxx` IN ('M001111122')";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){
                System.out.println("sLogNamex = " + instance.Decrypt(loRS.getString("sLogNamex")));
                System.out.println("sPassword = " + instance.Decrypt(loRS.getString("sPassword")));
                System.out.println("sUserName = " + instance.Decrypt(loRS.getString("sUserName")));
                
                System.out.println("sLogNamex = " + MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sLogNamex")), "08220326"));
                System.out.println("sPassword = " + MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sPassword")), "08220326"));
                System.out.println("sUserName = " + MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sUserName")), "08220326"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
