package trd.home.tcg.constant;

import java.util.Objects;
import trd.home.common.logging.LogMethodCall;

public enum CardFoilType {
    ETCHED_FOIL,
    FOIL,
    HALO_FOIL,
    MASTERPIECE_FOIL,
    NO,
    OIL_SLICK_RAISED_FOIL,
    STEP_AND_COMPLEAT_FOIL,
    SURGE_FOIL,
    TEXTURED_FOIL;

    @LogMethodCall
    public static boolean isFoil(CardFoilType type) {
        return Objects.nonNull(type) && type != NO;
    }

    @LogMethodCall
    public static CardFoilType fromString(String value) {
        for (CardFoilType type : CardFoilType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
