package trd.home.tcg.constant;

import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardGameType {
    MAGIC_THE_GATHERING("Magic"),
    FLESH_AND_BLOOD("FleshAndBlood");

    private final String cardmarketUrlPart;

    public static CardGameType findByCardmarketUrlPart(String cardmarketUrlPart) {
        return Stream.of(CardGameType.values())
                .filter(type -> type.cardmarketUrlPart.equals(cardmarketUrlPart))
                .findFirst()
                .orElse(null);
    }
}
