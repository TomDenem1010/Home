package trd.home.media.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.media.exception.InvalidMediaPathException;

class MediaImportServiceTest {

    private final MediaFileReader reader = mock(MediaFileReader.class);
    private final MediaPersistenceService persistence = mock(MediaPersistenceService.class);
    private final MediaImportService service = new MediaImportService(reader, persistence);

    @Test
    void readsAndPersistsImportedPath() {
        Path path = Path.of("media");
        when(reader.read(path)).thenReturn(List.of());
        when(persistence.save(List.of())).thenReturn(3);

        assertEquals(3, service.importPath("media"));
        verify(reader).read(path);
        verify(persistence).save(List.of());
    }

    @Test
    void rejectsMissingPath() {
        for (String path : new String[] {null, "", "  "}) {
            assertThrows(InvalidMediaPathException.class, () -> service.importPath(path));
        }
        verifyNoInteractions(reader, persistence);
    }

    @Test
    void wrapsInvalidOperatingSystemPath() {
        assertThrows(InvalidMediaPathException.class, () -> service.importPath("bad\u0000path"));
        verifyNoInteractions(reader, persistence);
    }
}
