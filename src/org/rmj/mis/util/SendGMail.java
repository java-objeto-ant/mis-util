package org.rmj.mis.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.rmj.appdriver.agentfx.CommonUtils;

public class SendGMail {
    //args[0] - receivier
    //args[1] - subject
    //args[2] - message
    public static void main(String[] args) {
        if (args.length != 3) System.exit(1);
        //if (!CommonUtils.isValidEmail(args[0])) System.exit(1);
        
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
                    InternetAddress.parse(args[0])
            );
            message.setSubject(args[1]);
            message.setText(args[2]);

            Transport.send(message);
            System.exit(0);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
