package trd.home.tcg.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import trd.home.tcg.exception.WrongDeckNameException;

class ResourceDeckNameValidatorTest {

    private final ResourceDeckNameValidator validator = new ResourceDeckNameValidator();

    @ParameterizedTest
    @ValueSource(strings = {"KiloApogeeMind_v1.csv", "mydeck_v2.csv"})
    void acceptsCsvFilenamesContainingDeckNameAndVersion(String filename) {
        assertDoesNotThrow(() -> validator.validateResource(resource(filename)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"deck.csv", "deck_v1.txt", "deck_name_v1.csv"})
    void rejectsInvalidDeckFilenames(String filename) {
        assertThrows(WrongDeckNameException.class, () -> validator.validateResource(resource(filename)));
    }

    private static ByteArrayResource resource(String filename) {
        return new ByteArrayResource(new byte[0]) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
