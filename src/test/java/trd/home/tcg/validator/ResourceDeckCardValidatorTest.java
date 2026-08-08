package trd.home.tcg.validator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import trd.home.tcg.exception.UnableToReadResourcesException;
import trd.home.tcg.exception.WrongCardLineException;

class ResourceDeckCardValidatorTest {

    private final ResourceDeckCardValidator validator = new ResourceDeckCardValidator();

    @Test
    void acceptsValidCardLines() {
        assertDoesNotThrow(() -> validator.validateResource(resource("1,https://www.cardmarket.com/card,FOIL")));
    }

    @Test
    void reportsPhysicalLineNumberForInvalidCardLine() {
        WrongCardLineException exception = assertThrows(
                WrongCardLineException.class,
                () -> validator.validateResource(resource("1,https://www.cardmarket.com/card,FOIL\n\ninvalid")));

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("line 3"));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(
                WrongCardLineException.class,
                () -> validator.validateResource(resource("0,https://www.cardmarket.com/card,FOIL")));
    }

    @Test
    void rejectsUnknownFoilType() {
        assertThrows(
                WrongCardLineException.class,
                () -> validator.validateResource(resource("1,https://www.cardmarket.com/card,UNKNOWN")));
    }

    @Test
    void wrapsResourceReadFailure() {
        assertThrows(UnableToReadResourcesException.class, () -> validator.validateResource(unreadableResource()));
    }

    @Test
    void rejectsInvalidCardmarketLink() {
        assertThrows(
                WrongCardLineException.class,
                () -> validator.validateResource(resource("1,https://example.com/card,FOIL")));
    }

    private static ByteArrayResource resource(String content) {
        return new ByteArrayResource(content.getBytes(UTF_8)) {
            @Override
            public String getFilename() {
                return "Kilo_v1.csv";
            }
        };
    }

    private static ByteArrayResource unreadableResource() {
        return new ByteArrayResource(new byte[0]) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("Unable to read");
            }

            @Override
            public String getFilename() {
                return "Kilo_v1.csv";
            }
        };
    }
}
