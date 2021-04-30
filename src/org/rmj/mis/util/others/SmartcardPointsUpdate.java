/*package org.rmj.mis.util.others;


import org.rmj.gcard.base.misc.GCEncoder;

public class SmartcardPointsUpdate {
    public static void main (String [] args){
        if(!GCEncoder.init()){
            System.err.println("read: Can't Initialize card. " + GCEncoder.getErrMessage());
            System.exit(1);
        }//end: if(!GCEncoder.init())

        if(!GCEncoder.connect()){
            System.err.println("connectCard: Can't Connect card. " + GCEncoder.getErrMessage());
            System.exit(1);
        }//end: if(!GCEncoder.connect())

        String lsPin1 =  String.valueOf(((String) GCEncoder.read(GCEncoder.RESERVED3)).getBytes()[0]);
        String lsPin2 =  String.valueOf(((String) GCEncoder.read(GCEncoder.RESERVED5)).getBytes()[0]);

        String lsGCardNmbr = (String) GCEncoder.read(GCEncoder.CARD_NUMBER);

        if(!GCEncoder.verifyPSC(lsPin1, lsPin2)){
            System.err.println("connectCard: Unable to verify pin number " + lsGCardNmbr + "!");
            System.exit(1);
        }//end: if(!GCEncoder.verifyPSC(lsPin1, lsPin2))

        //release device
        //GCEncoder.disconnect();
        
        long points = (long) GCEncoder.read(GCEncoder.POINTS);
        
        System.out.println("ORIG POINTS: " + points);
        
        
        GCEncoder.write(GCEncoder.POINTS, (long) (750));
        
        points = (long) GCEncoder.read(GCEncoder.POINTS);
        
        System.out.println("NEW POINTS: " + points);
        
        GCEncoder.disconnect();
    }
}*/