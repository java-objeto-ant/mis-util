package org.rmj.mis.util.gapp;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agent.GRiderX;

public class SendEmailVerification {
    final static String SEND_VERIFICATION = "security/send-verification.php";
    
    public static void main(String [] args){
        final String PRODUCTID = "IntegSys";
        final String USERID = "M001111122";
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRiderX poGRider = new GRiderX("IntegSys");

        if (!poGRider.loadEnv(PRODUCTID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        if (!poGRider.logUser(PRODUCTID, USERID)) {
            System.err.println(poGRider.getMessage());
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
                
        String lsSQL = "SELECT *" + 
                        " FROM App_User_Master" + 
                        " WHERE sProdctID IN ('gRider', 'IntegSys')" +
                            " AND cEmailSnt <> '1'" + 
                            " AND cActivatd <> '1'" +
                            " AND dCreatedx >= '2023-08-01'" +
                        " ORDER BY dCreatedx DESC" +
                        " LIMIT 50";
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            String to;
            String message;
            
            while (loRS.next()){
                to = loRS.getString("sEmailAdd");
                message = "Thank you for signing up!\n" +
                            "Your account has been created. Please click the link below to activate your account.\n\n" +
                            "https://restgk.guanzongroup.com.ph/security/account_verify.php?email=" + loRS.getString("sEmailAdd") + "&hash=" + loRS.getString("sItIsASIN");
                
                
                if (sendmail(to, "Signup | Verification", message)){
                    lsSQL = "UPDATE App_User_Master SET" +
                                "  cEmailSnt = '1'" +
                                ", nEmailSnt = 1" +
                            " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx"));
                    System.out.println(lsSQL);
                    poGRider.executeUpdate(lsSQL);
                }
            }
            System.exit(0);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
    
    private static boolean sendmail(String to, String subject, String body){
        final String username = "noreply.guanzongroup@gmail.com";
        final String password = "lsrmdimuftovtnwh";     
        
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply.guanzongroup@gmail.com", "Guanzon"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
}
