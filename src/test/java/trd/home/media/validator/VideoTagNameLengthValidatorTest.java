package trd.home.media.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import trd.home.media.exception.InvalidVideoTagException;

class VideoTagNameLengthValidatorTest {
    private final VideoTagNameLengthValidator validator = new VideoTagNameLengthValidator();

    @ParameterizedTest
    @ValueSource(ints = {1, 254, 255})
    void acceptsNamesWithinLimit(int length) {
        assertDoesNotThrow(() -> validator.validate("A".repeat(length)));
    }

    @ParameterizedTest
    @ValueSource(ints = {256, 1000})
    void rejectsNamesExceedingLimit(int length) {
        var failure = assertThrows(InvalidVideoTagException.class, () -> validator.validate("A".repeat(length)));
        assertEquals("Video tag name must not exceed 255 characters.", failure.getMessage());
    }
}
