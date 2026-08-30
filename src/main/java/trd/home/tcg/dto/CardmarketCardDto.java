package trd.home.tcg.dto;

import java.net.URI;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardLanguage;
import trd.home.tcg.dao.CardmarketCard;

@Slf4j
public record CardmarketCardDto(
        String id,
        String link,
        CardFoilType foilType,
        CardGameType cardGameType,
        String expansion,
        String name,
        CardLanguage cardLanguage) {

    @LogMethodCall
    public static CardmarketCardDto from(CardmarketCard card) {
        String link = card.getLink();
        if (Objects.isNull(link) || link.isBlank()) {
            return new CardmarketCardDto(card.getId(), link, card.getFoilType(), null, null, null, null);
        }

        URI uri = URI.create(link.trim());
        String[] parts = uri.getPath().split("/");
        int productsIndex = findPartIndex(parts, "Products");
        int singlesIndex = findPartIndex(parts, "Singles");

        CardGameType cardGameType =
                productsIndex > 0 ? CardGameType.findByCardmarketUrlPart(parts[productsIndex - 1]) : null;
        String expansion = singlesIndex >= 0 && parts.length > singlesIndex + 1 ? parts[singlesIndex + 1] : null;
        String name = singlesIndex >= 0 && parts.length > singlesIndex + 2 ? parts[singlesIndex + 2] : null;
        CardLanguage cardLanguage = findLanguage(uri.getQuery());

        return new CardmarketCardDto(
                card.getId(), link, card.getFoilType(), cardGameType, expansion, name, cardLanguage);
    }

    private static int findPartIndex(String[] parts, String expectedPart) {
        for (int index = 0; index < parts.length; index++) {
            if (expectedPart.equals(parts[index])) {
                return index;
            }
        }
        return -1;
    }

    private static CardLanguage findLanguage(String query) {
        if (Objects.isNull(query)) {
            return null;
        }

        for (String parameter : query.split("&")) {
            String[] keyValue = parameter.split("=", 2);
            if (keyValue.length == 2 && "language".equals(keyValue[0])) {
                try {
                    return CardLanguage.findByCardmarketUrlPart(Integer.parseInt(keyValue[1]));
                } catch (NumberFormatException exception) {
                    log.error("Failed to parse Cardmarket language identifier '{}'", keyValue[1], exception);
                    return null;
                }
            }
        }
        return null;
    }
}
