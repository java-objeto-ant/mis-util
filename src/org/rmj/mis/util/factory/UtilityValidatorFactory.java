package org.rmj.mis.util.factory;

import org.rmj.mis.util.hcm.Batch_Shift_Movement;

public class UtilityValidatorFactory {
    public enum UtilityType{
        CLIENT_MOBILE,
        API_PAYMENTS,
        TLM_PRIMARY_LEADS,
        TLM_MCSO_AS_MP_LEADS,
        TLM_CA_LEADS,
        CLIENT_OCCUPATION,
        MONITORING_BOARD, 
        CLASSIFY_MOBILE,
        BATCH_SHIFT_MOVEMENT,
        HOLIDAY_GREETINGS
    }
    
    public static UtilityValidator make(UtilityValidatorFactory.UtilityType foType){
        switch (foType){
            case CLIENT_MOBILE:
                return new Client_Mobile();
            case API_PAYMENTS:
                return new API_Payments();
            case TLM_PRIMARY_LEADS:
                return new TLM_Leads1();
            case TLM_MCSO_AS_MP_LEADS:
                return new TLM_MCSO1();
            case TLM_CA_LEADS:
                return new TLM_CA();
            case CLIENT_OCCUPATION:
                return new Client_Occupation();
            case MONITORING_BOARD:
                return new Monitoring_Board();
            case CLASSIFY_MOBILE:
                return new Classify_Mobile();
            case BATCH_SHIFT_MOVEMENT:
                return new Batch_Shift_Movement();
            default:
                return null;
        }
    }
}