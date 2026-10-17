package trd.home.media.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import trd.home.media.exception.InvalidVideoTagException;

class VideoTagNameRequiredValidatorTest {
    private final VideoTagNameRequiredValidator validator = new VideoTagNameRequiredValidator();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", "\u2003"})
    void rejectsMissingName(String name) {
        var failure = assertThrows(InvalidVideoTagException.class, () -> validator.validate(name));
        assertEquals("Video tag name is required.", failure.getMessage());
    }

    @Test
    void acceptsNonBlankName() {
        assertDoesNotThrow(() -> validator.validate("TRAVEL"));
    }
}
