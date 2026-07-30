package trd.home.tcg.dto;

import java.net.URI;
import java.util.Objects;

import lombok.Getter;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardLanguage;

@Getter
public class CardmarketCardDto {

    private final String id;
    private final String link;
    private final CardGameType cardGameType;
    private final String expansion;
    private final String name;
    private final CardLanguage cardLanguage;

    public CardmarketCardDto(String id, String link) {
        this.id = id;
        this.link = link;

        if (Objects.isNull(link) || link.trim().isEmpty()) {
            this.expansion = null;
            this.name = null;
            this.cardLanguage = null;
            this.cardGameType = null;
            return;
        } else {
            URI uri = URI.create(link.trim());
            String[] parts = uri.getPath().split("/");
            int productsIndex = findPartIndex(parts, "Products");
            int singlesIndex = findPartIndex(parts, "Singles");

            this.cardGameType = productsIndex > 0
                    ? CardGameType.findByCardmarketUrlPart(parts[productsIndex - 1])
                    : null;
            this.expansion = singlesIndex >= 0 && parts.length > singlesIndex + 1 ? parts[singlesIndex + 1] : null;
            this.name = singlesIndex >= 0 && parts.length > singlesIndex + 2 ? parts[singlesIndex + 2] : null;
            this.cardLanguage = findLanguage(uri.getQuery());
        }
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
                    return CardLanguage.findByCardmarketUrlPart(Integer.valueOf(keyValue[1]));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
