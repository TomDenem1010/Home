package trd.home.media.service.importing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import trd.home.media.dto.ParsedVideoName;

class MediaNameParserTest {
    private final MediaNameParser parser = new MediaNameParser();

    @Test
    void parsesValidExtensionlessHiddenVideoNameWhoseFirstCharacterIsADot() {
        ParsedVideoName result = parser.parse(".Actor - Film");

        assertEquals("Film", result.name());
        assertEquals(Set.of(".Actor"), result.actors());
    }

    @Test
    void parsesActorsAndPreservesSpacesAndTitleSeparators() {
        var result = parser.parse("Jane Doe & John Doe - A title - Part 2.MP4");
        assertEquals(Set.of("Jane Doe", "John Doe"), result.actors());
        assertEquals("A title - Part 2", result.name());
    }

    @Test
    void removesDuplicateActors() {
        assertEquals(
                Set.of("Jane Doe"),
                parser.parse("Jane Doe & Jane Doe - Title.mp4").actors());
    }

    @Test
    void rejectsMalformedActorSectionsAndEmptyTitles() {
        for (String name : List.of(" - Title.mp4", "Actor - .mp4", "Actor &  - Title.mp4", "", "   .mp4")) {
            assertThrows(trd.home.media.exception.InvalidVideoNameException.class, () -> parser.parse(name), name);
        }
    }

    @Test
    void acceptsTitlesWithoutActorsWithAndWithoutExtension() {
        for (String fileName : List.of("Film.mp4", "  Film  .MP4", "Film")) {
            var result = parser.parse(fileName);
            assertEquals("Film", result.name());
            assertTrue(result.actors().isEmpty());
        }
        assertEquals("Film&Part-2", parser.parse("Film&Part-2.mp4").name());
        assertEquals("x".repeat(255), parser.parse("x".repeat(255) + ".mp4").name());
        assertThrows(
                trd.home.media.exception.InvalidVideoNameException.class, () -> parser.parse("x".repeat(256) + ".mp4"));
    }

    @Test
    void identityIgnoresActorOrderAndSeparatesNamesUnambiguously() {
        assertEquals(
                MediaPersistenceService.actorKey(new LinkedHashSet<>(List.of("A", "B"))),
                MediaPersistenceService.actorKey(new LinkedHashSet<>(List.of("B", "A"))));
        assertNotEquals(
                MediaPersistenceService.actorKey(Set.of("AB", "C")),
                MediaPersistenceService.actorKey(Set.of("A", "BC")));
    }

    @Test
    void acceptsFilenameWithoutExtensionAndStripsWhitespace() {
        var result = parser.parse("  Alice  & Bob -   Film  ");

        assertEquals("Film", result.name());
        assertEquals(Set.of("Alice", "Bob"), result.actors());
    }

    @Test
    void rejectsOverlongTitleAndActor() {
        String overlong = "x".repeat(256);

        assertThrows(
                trd.home.media.exception.InvalidVideoNameException.class, () -> parser.parse("Alice - " + overlong));
        assertThrows(
                trd.home.media.exception.InvalidVideoNameException.class, () -> parser.parse(overlong + " - Film"));
    }

    @Test
    void acceptsBoundaryLengthTitleActorAndOneCharacterActor() {
        String boundary = "x".repeat(255);

        assertEquals("Title", parser.parse("A - Title").name());
        assertEquals(boundary, parser.parse("Alice - " + boundary).name());
        assertEquals(Set.of(boundary), parser.parse(boundary + " - Film").actors());
    }
}
