package trd.home.tcg.service.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import trd.home.common.logging.LogMethodCall;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.exception.UnableToReadResourcesException;

@Slf4j
@AllArgsConstructor
public class DeckFileReader extends ResourceFileReader {

    private static final String DECK_RESOURCE_PATTERN = "classpath*:tcg/deck/*.csv";

    private final List<ResourceValidator> resourceValidators;

    @LogMethodCall
    public List<CardmarketDeck> read() {
        return Arrays.stream(readResources(DECK_RESOURCE_PATTERN))
                .map(this::readDeck)
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
        try (var reader = resource.getInputStream()) {
            new String(reader.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .filter(line -> !line.isBlank())
                    .forEach(line -> addCard(version, line));
        } catch (IOException exception) {
            log.error("Failed to read cards from deck file '{}'", resource.getFilename(), exception);
            throw new UnableToReadResourcesException("Unable to read deck file: " + resource.getFilename(), exception);
        }
    }

    private static void addCard(CardmarketDeckVersion version, String line) {
        String[] values = line.split(",", -1);
        CardmarketCard card = new CardmarketCard();
        card.setLink(values[1]);
        card.setFoilType(CardFoilType.valueOf(values[2]));
        version.addCard(card, Integer.parseInt(values[0]));
    }
}
