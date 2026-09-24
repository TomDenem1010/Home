package trd.home.tcg.validator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import trd.home.common.exception.ResourceReadException;
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

        assertThrows(ResourceReadException.class, () -> validator.validateResource(resource));
    }

    @Test
    void closesResourceStreamAfterValidation() {
        AtomicBoolean closed = new AtomicBoolean();
        ByteArrayResource resource = new ByteArrayResource("deck".getBytes(UTF_8)) {
            @Override
            public InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(getByteArray()) {
                    @Override
                    public void close() throws IOException {
                        closed.set(true);
                        super.close();
                    }
                };
            }
        };

        validator.validateResource(resource);

        org.junit.jupiter.api.Assertions.assertTrue(closed.get());
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
