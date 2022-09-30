package org.rmj.mis.util.raffle;

public class RaffleValidatorFactory {
    public enum UtilityType{
        MOBILE_PHONE_SALES,
        MOTORCYCLE_SALES,
        LR_PAYMENT
    }
    
    public static RaffleValidator make(RaffleValidatorFactory.UtilityType foType){
        switch (foType){
            case MOTORCYCLE_SALES:
                return new ExtractMCSales();
            case MOBILE_PHONE_SALES:
                return new ExtractMPSales();
            case LR_PAYMENT:
                return new ExtractLR();
            default:
                return null;
        }
    }
}