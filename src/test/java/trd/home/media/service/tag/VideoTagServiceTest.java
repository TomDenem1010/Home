package trd.home.media.service.tag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.media.exception.InvalidVideoTagException;
import trd.home.media.repository.VideoTagRepository;
import trd.home.media.validator.VideoTagNameValidator;

class VideoTagServiceTest {
    private final VideoTagRepository repository = mock(VideoTagRepository.class);
    private final VideoTagNameValidator first = mock(VideoTagNameValidator.class);
    private final VideoTagNameValidator second = mock(VideoTagNameValidator.class);
    private final VideoTagService service = new VideoTagService(repository, List.of(first, second));

    @Test
    void passesUppercaseTrimmedNameToEveryValidatorBeforeSaving() {
        service.create("  Travel  ");
        var order = inOrder(first, second, repository);
        order.verify(first).validate("TRAVEL");
        order.verify(second).validate("TRAVEL");
        order.verify(repository).save(argThat(tag -> tag.getName().equals("TRAVEL")));
    }

    @Test
    void passesNullToValidatorsWithoutSavingWhenRejected() {
        var failure = new InvalidVideoTagException("Name is required");
        doThrow(failure).when(first).validate(null);
        assertSame(failure, assertThrows(InvalidVideoTagException.class, () -> service.create(null)));
        verifyNoInteractions(second, repository);
    }

    @Test
    void stopsValidationAndDoesNotSaveWhenValidatorRejectsName() {
        var failure = new InvalidVideoTagException("Invalid name");
        doThrow(failure).when(first).validate("TRAVEL");
        assertSame(failure, assertThrows(InvalidVideoTagException.class, () -> service.create("Travel")));
        verifyNoInteractions(second, repository);
    }

    @Test
    void passesExpandedUppercaseNameToValidators() {
        service.create("\u00df".repeat(128));
        verify(first).validate("SS".repeat(128));
        verify(second).validate("SS".repeat(128));
    }

    @Test
    void propagatesPersistenceFailure() {
        var failure = new IllegalStateException("Database unavailable");
        when(repository.save(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.create("Travel")));
    }
}
