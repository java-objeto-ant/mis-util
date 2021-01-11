package org.rmj.mis.util.factory;

public class UtilityValidatorFactory {
    public enum UtilityType{
        CLIENT_MOBILE,
        API_PAYMENTS,
        TLM_PRIMARY_LEADS,
        TLM_MCSO_AS_MP_LEADS,
        TLM_CA_LEADS
    }
    
    public static UtilityValidator make(UtilityValidatorFactory.UtilityType foType){
        switch (foType){
            case CLIENT_MOBILE:
                return new Client_Mobile();
            case API_PAYMENTS:
                return new API_Payments();
            case TLM_PRIMARY_LEADS:
                return new TLM_Leads();
            case TLM_MCSO_AS_MP_LEADS:
                return new TLM_MCSO();
            case TLM_CA_LEADS:
                return new TLM_CA();
            default:
                return null;
        }
    }
}
