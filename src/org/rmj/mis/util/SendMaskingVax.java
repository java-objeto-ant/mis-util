//package org.rmj.mis.util;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.Scanner;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.rmj.appdriver.GRider;
//import org.rmj.lib.net.LogWrapper;
//import org.rmj.mis.util.sms.MaskSMS;
//
//public class SendMaskingVax {
//    public static void main(String [] args){        
//        LogWrapper logwrapr = new LogWrapper("masking", "textblast.log");
//        logwrapr.info("Start of Process!");
//        
//        String path;
//        if(System.getProperty("os.name").toLowerCase().contains("win")){
//            path = "D:/GGC_Java_Systems";
//        }
//        else{
//            path = "/srv/GGC_Java_Systems";
//        }
//        System.setProperty("sys.default.path.config", path);
//        
//        GRider instance = new GRider("IntegSys");
//        
//        if (!instance.logUser("IntegSys", "M001111122")){
//            System.err.println(instance.getMessage() + instance.getErrMsg());
//            logwrapr.severe(instance.getMessage() + instance.getErrMsg());
//            System.exit(1);
//        }
//        
////        String message = "Good day Ma'am/Sir <<name>>, may we remind you of your 2nd COVID-19 vaccine (AstraZeneca) at the Dagupan City People's Astrodome on November 13, 2021. Please bring: 1. Ballpen, 2. Any ID, 3. Vaccination card/printout, 4. Wear face mask & face shield. If you won't make it on your scheduled date, pls. inform this # 09778236651.\n" +
////                        "Thank you very much.";
//        String message = "Good day! Ma'am/Sir <<name>> , we would like to inform you that your booster shot of COVID-19 Vaccine (AstraZeneca) is scheduled on Dec 4, 2021, 8:30 AM at Dagupan City Peoples Astrodome. Please bring the following: 1. Ballpen, 2. Any ID, 3. Wear face mask, 4. Vaccination card/printout. If you won't make it on your scheduled date, please inform this number, 09778236651. Thank you very much.";
//        
//        MaskSMS processor = new MaskSMS(instance, logwrapr);
//        processor.setMaskName("GUANZON");
//        
//        JSONArray laRecipients = getRecipientsWithName();
//        JSONObject loJSON;
//        
//        ArrayList<String> number;
//        String messagex;
//        
//        for (int lnCtr = 0; lnCtr <= laRecipients.size() -1; lnCtr ++){
//            loJSON = (JSONObject) laRecipients.get(lnCtr);
//            
//            messagex = message.replace("<<time>>", (String) loJSON.get("time"));
//            messagex = messagex.replace("<<name>>", (String) loJSON.get("name"));
//            System.out.println(messagex);
//            
//            number = new ArrayList<>();
//            number.add((String) loJSON.get("mobile"));
//            
//            processor.setSMS(messagex);
//            processor.setRecipient(number);
//
//            if (!processor.sendMessage()){
//                logwrapr.severe(processor.getMessage());
//                System.exit(1);
//            }
//        }
//        
//        logwrapr.info("SMS sending done. Thank you.");
//        System.exit(0);
//    }
//    
//    public static JSONArray getRecipientsWithName(){
//        JSONArray loArray = new JSONArray();
//        JSONObject loJSON;
//        
//        try {
//            File myObj = new File("vaccine11.txt");
//            Scanner myReader = new Scanner(myObj);
//            
//            while (myReader.hasNextLine()) {
//                String data = myReader.nextLine();
//                String [] datax = data.split(";");
//
//                loJSON = new JSONObject();
//                loJSON.put("time", datax[0]);
//                loJSON.put("mobile", datax[1]);
//                loJSON.put("name", datax[2]);
//                loArray.add(loJSON);
//            }
//            
//            myReader.close();
//        } catch (FileNotFoundException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//        
//        System.err.println(loArray.toJSONString());
//        return loArray;
//    }
//}
