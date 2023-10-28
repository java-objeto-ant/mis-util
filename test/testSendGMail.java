
import org.rmj.mis.util.SendGMail;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author user
 */
public class testSendGMail {
    public static void main (String [] args){
        String param[] = new String[3];
        param[0] = "michael_cuison07@yahoo.com";
        param[1] = "Test Subject";
        param[2] = "Test Body";
        
        
        SendGMail.main(param);
    }
}
