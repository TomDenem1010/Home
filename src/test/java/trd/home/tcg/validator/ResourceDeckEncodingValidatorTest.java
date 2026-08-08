package trd.home.tcg.validator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import trd.home.tcg.exception.UnableToReadResourcesException;
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

    @Test
    void wrapsResourceReadFailure() {
        ByteArrayResource resource = new ByteArrayResource(new byte[0]) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("Unable to read");
            }

            @Override
            public String getFilename() {
                return "Kilo_v1.csv";
            }
        };

        assertThrows(UnableToReadResourcesException.class, () -> validator.validateResource(resource));
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
