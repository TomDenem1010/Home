package trd.home.tcg.service.file;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import trd.home.common.file.ResourceFileReader;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;

@AllArgsConstructor
public class DeckFileReader extends ResourceFileReader {

    private static final String DECK_RESOURCE_PATTERN = "classpath*:tcg/deck/*.csv";

    private final List<ResourceValidator> resourceValidators;

    public List<CardmarketDeck> read() {
        return readResources(DECK_RESOURCE_PATTERN).stream()
                .map(resource -> readDeck(resource))
                .toList();
    }

    private CardmarketDeck readDeck(Resource resource) {
        resourceValidators.forEach(validator -> validator.validateResource(resource));

        String filename = resource.getFilename();
        int versionSeparator = filename.lastIndexOf('_');

        CardmarketDeck deck = new CardmarketDeck();
        deck.setName(filename.substring(0, versionSeparator));

        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setVersion(filename.substring(versionSeparator + 1, filename.length() - ".csv".length()));
        addCardsToVersion(version, resource);

        deck.addVersion(version);
        return deck;
    }

    private void addCardsToVersion(CardmarketDeckVersion version, Resource resource) {
        readUtf8Lines(resource).stream().filter(line -> !line.isBlank()).forEach(line -> addCard(version, line));
    }

    private static void addCard(CardmarketDeckVersion version, String line) {
        String[] values = line.split(",", -1);
        CardmarketCard card = new CardmarketCard();
        card.setLink(values[1]);
        card.setFoilType(CardFoilType.valueOf(values[2]));
        version.addCard(card, Integer.parseInt(values[0]));
    }
}
