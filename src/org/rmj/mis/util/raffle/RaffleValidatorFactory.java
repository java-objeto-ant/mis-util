package org.rmj.mis.util.raffle;

public class RaffleValidatorFactory {
    public enum UtilityType{
        MOBILE_PHONE_SALES,
        MOTORCYCLE_SALES,
        SPAREPARTS_SALES,
        LR_PAYMENT,
        LR_PAYMENT_PR,
        OFFICIAL_RECEIPT,
        PROVISIONARY_RECEIPT,
        OTHER_DIVISION,
        NOTIFIER,
        FEEDBACK
    }
    
    public static RaffleValidator make(RaffleValidatorFactory.UtilityType foType){
        switch (foType){
            case MOTORCYCLE_SALES:
                return new ExtractMCSales();
            case MOBILE_PHONE_SALES:
                return new ExtractMPSales();
            case SPAREPARTS_SALES:
                return new ExtractSPSales();
            case LR_PAYMENT:
                return new ExtractLR();
            case LR_PAYMENT_PR:
                return new ExtractLRPR();
            case OFFICIAL_RECEIPT:
                return new ExtractOR();
            case PROVISIONARY_RECEIPT:
                return new ExtractPR();
            case NOTIFIER:
                return new RaffleNotify();
            case FEEDBACK:
                return new RaffleFeedback();
            case OTHER_DIVISION:
                return new ExtractOtherDivision();
            default:
                return null;
        }
    }
}