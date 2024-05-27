
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.MySQLAESCrypt;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class testMigrateUserAccounts {
    public static void main(String [] args){
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
        
        String lsSQL = "SELECT * FROM xxxSysApplication;";
        ResultSet loRS = instance.executeQuery(lsSQL);       
        
        try {
            while (loRS.next()){
                System.out.println(instance.Decrypt(loRS.getString("sSysAdmin")));
                System.out.println(MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sSysAdmin")), "08220326"));
                
                System.out.println(instance.Decrypt(loRS.getString("sNetWarex")));
                System.out.println(MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sNetWarex")), "08220326"));
                
                System.out.println(instance.Decrypt(loRS.getString("sMachinex")));
                System.out.println(MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sMachinex")), "08220326"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
