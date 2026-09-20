package trd.home.tcg.service.file;

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
import trd.home.common.exception.ResourceReadException;
import trd.home.common.validator.ResourceValidator;
import trd.home.tcg.dao.CardmarketDeck;

class DeckFileReaderTest {

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

        assertThrows(ResourceReadException.class, reader::read);
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

        private final List<Resource> resources;

        TestDeckFileReader(List<ResourceValidator> validators, Resource... resources) {
            super(validators);
            this.resources = List.of(resources);
        }

        @Override
        protected List<Resource> readResources(String resourcePattern) {
            return resources;
        }
    }
}
