package trd.home.tcg.dto;

import java.net.URI;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardLanguage;
import trd.home.tcg.dao.CardmarketCard;

@Slf4j
public record CardmarketCardDto(
        @NonNull String id,
        @NonNull String link,
        @NonNull CardFoilType foilType,
        @NonNull CardGameType cardGameType,
        @NonNull String expansion,
        @NonNull String name,
        @NonNull CardLanguage cardLanguage) {

    public static CardmarketCardDto from(CardmarketCard card) {
        String link = card.getLink();
        if (Objects.isNull(link) || link.isBlank()) {
            return new CardmarketCardDto(
                    Objects.requireNonNullElse(card.getId(), ""),
                    "",
                    Objects.requireNonNullElse(card.getFoilType(), CardFoilType.NO),
                    CardGameType.MAGIC_THE_GATHERING,
                    "",
                    "",
                    CardLanguage.ENGLISH);
        }

        URI uri = URI.create(link.trim());
        String[] parts = uri.getPath().split("/");
        int productsIndex = findPartIndex(parts, "Products");
        int singlesIndex = findPartIndex(parts, "Singles");

        CardGameType cardGameType = productsIndex > 0
                ? Objects.requireNonNullElse(
                        CardGameType.findByCardmarketUrlPart(parts[productsIndex - 1]),
                        CardGameType.MAGIC_THE_GATHERING)
                : CardGameType.MAGIC_THE_GATHERING;
        String expansion = singlesIndex >= 0 && parts.length > singlesIndex + 1 ? parts[singlesIndex + 1] : "";
        String name = singlesIndex >= 0 && parts.length > singlesIndex + 2 ? parts[singlesIndex + 2] : "";
        CardLanguage cardLanguage = findLanguage(uri.getQuery());

        return new CardmarketCardDto(
                Objects.requireNonNullElse(card.getId(), ""),
                link,
                Objects.requireNonNullElse(card.getFoilType(), CardFoilType.NO),
                cardGameType,
                expansion,
                name,
                cardLanguage);
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
            return CardLanguage.ENGLISH;
        }

        for (String parameter : query.split("&")) {
            String[] keyValue = parameter.split("=", 2);
            if (keyValue.length == 2 && "language".equals(keyValue[0])) {
                try {
                    return Objects.requireNonNullElse(
                            CardLanguage.findByCardmarketUrlPart(Integer.parseInt(keyValue[1])), CardLanguage.ENGLISH);
                } catch (NumberFormatException exception) {
                    log.error("Failed to parse Cardmarket language identifier '{}'", keyValue[1], exception);
                    return CardLanguage.ENGLISH;
                }
            }
        }
        return CardLanguage.ENGLISH;
    }
}
