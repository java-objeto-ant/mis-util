
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MySQLAESCrypt;
import org.rmj.appdriver.constants.UserRight;


public class testCreateCASAccount {
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
        
        try {
            String user = MySQLAESCrypt.Encrypt(instance.Decrypt("Shan1"), "08220326");
            int level = UserRight.ENCODER;
            
            String lsSQL = "";
            
                System.out.println("sLogNamex = " + MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sLogNamex")), "08220326"));
                System.out.println("sPassword = " + MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sPassword")), "08220326"));
                System.out.println("sUserName = " + MySQLAESCrypt.Encrypt(instance.Decrypt(loRS.getString("sUserName")), "08220326"));
            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
