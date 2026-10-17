package trd.home.media.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import trd.home.media.exception.InvalidVideoTagException;
import trd.home.media.repository.VideoTagRepository;

class VideoTagNameUniqueValidatorTest {
    private final VideoTagRepository repository = mock(VideoTagRepository.class);
    private final VideoTagNameUniqueValidator validator = new VideoTagNameUniqueValidator(repository);

    @Test
    void acceptsNewName() {
        when(repository.existsByName("TRAVEL")).thenReturn(false);
        assertDoesNotThrow(() -> validator.validate("TRAVEL"));
        verify(repository).existsByName("TRAVEL");
    }

    @Test
    void rejectsExistingName() {
        when(repository.existsByName("TRAVEL")).thenReturn(true);
        var failure = assertThrows(InvalidVideoTagException.class, () -> validator.validate("TRAVEL"));
        assertEquals("A video tag with this name already exists.", failure.getMessage());
    }

    @Test
    void propagatesRepositoryFailure() {
        var failure = new IllegalStateException("Database unavailable");
        when(repository.existsByName("TRAVEL")).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> validator.validate("TRAVEL")));
    }
}
