
import org.rmj.appdriver.GRider;


public class testAutoAppLogin {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("AutoApp");
        
        if (!instance.logUser("AutoApp", "M001111122")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }
    }
}
