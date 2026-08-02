package trd.home.tcg.validator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import trd.home.tcg.exception.WrongDeckEncodingException;

class ResourceDeckEncodingValidatorTest {

    private final ResourceDeckEncodingValidator validator = new ResourceDeckEncodingValidator();

    @Test
    void acceptsValidUtf8Content() {
        assertDoesNotThrow(() -> validator.validateResource(resource("Kilo_v1.csv", "Kártya".getBytes(UTF_8))));
    }

    @Test
    void rejectsInvalidUtf8Content() {
        assertThrows(
                WrongDeckEncodingException.class,
                () -> validator.validateResource(resource("Kilo_v1.csv", new byte[] {(byte) 0xC3, 0x28})));
    }

    private static ByteArrayResource resource(String filename, byte[] content) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
