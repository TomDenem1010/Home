package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.exception.UnableToReadResourcesException;

class DeckFileReaderTest {

    @Test
    void mapsDeckResourceToDeckAndCurrentVersion() {
        DeckFileReader reader = new DeckFileReader(List.of());

        List<CardmarketDeck> decks = reader.read();

        CardmarketDeck deck = decks.getFirst();
        CardmarketDeckCard firstCard =
                deck.getCurrentVersion().getCards().iterator().next();
        assertAll(
                () -> assertEquals(1, decks.size()),
                () -> assertEquals("KiloApogeeMind", deck.getName()),
                () -> assertEquals("v1", deck.getCurrentVersion().getVersion()),
                () -> assertEquals(99, deck.getCurrentVersion().getCards().size()),
                () -> assertEquals(1, firstCard.getQuantity()),
                () -> assertEquals(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Commander-Edge-of-Eternities/Kilo-Apogee-Mind?language=1&isFoil=Y",
                        firstCard.getCard().getLink()),
                () -> assertEquals(CardFoilType.FOIL, firstCard.getCard().getFoilType()));
    }

    @Test
    void validatesResourceAndIgnoresBlankLines() {
        ResourceValidator validator = mock(ResourceValidator.class);
        Resource resource = resource("1,https://www.cardmarket.com/card,FOIL\n\n");
        DeckFileReader reader = new TestDeckFileReader(List.of(validator), resource);

        List<CardmarketDeck> decks = reader.read();

        verify(validator).validateResource(resource);
        assertEquals(1, decks.size());
        assertEquals(1, decks.getFirst().getCurrentVersion().getCards().size());
    }

    @Test
    void wrapsCardResourceReadFailure() {
        Resource resource = new ByteArrayResource(new byte[0]) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("Unable to read");
            }

            @Override
            public String getFilename() {
                return "TestDeck_v1.csv";
            }
        };
        DeckFileReader reader = new TestDeckFileReader(List.of(), resource);

        assertThrows(UnableToReadResourcesException.class, reader::read);
    }

    private static Resource resource(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "TestDeck_v1.csv";
            }
        };
    }

    private static class TestDeckFileReader extends DeckFileReader {

        private final Resource[] resources;

        TestDeckFileReader(List<ResourceValidator> validators, Resource... resources) {
            super(validators);
            this.resources = resources;
        }

        @Override
        Resource[] readResources(String resourcePattern) {
            return resources;
        }
    }
}
