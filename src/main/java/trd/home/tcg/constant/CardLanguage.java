package trd.home.tcg.constant;

import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import trd.home.common.logging.LogMethodCall;

@Getter
@AllArgsConstructor
public enum CardLanguage {
    ENGLISH(1);

    private final Integer cardmarketUrlPart;

    @LogMethodCall
    public static CardLanguage findByCardmarketUrlPart(Integer cardmarketUrlPart) {
        return Stream.of(CardLanguage.values())
                .filter(language -> language.cardmarketUrlPart.equals(cardmarketUrlPart))
                .findFirst()
                .orElse(null);
    }
}
