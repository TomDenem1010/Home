package trd.home.frontend.boardgame;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.thymeleaf.context.Context;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import trd.home.boardgame.constant.*;
import trd.home.boardgame.dto.*;

class BoardGameTemplateTest {
    private final BoardGameDto game = new BoardGameDto(
            "game",
            "Azul <script>",
            "https://boardgamegeek.com/boardgame/230802/azul",
            BoardGameState.SOLD,
            BoardGameType.BASE);

    @Test
    void rendersGameListWithEscapedNamesBggLinksAndPaging() {
        var html = render(
                "games",
                Map.of(
                        "filter",
                        new BoardGameSearchFilter("Azul", null, null),
                        "results",
                        new PageImpl<>(List.of(game), PageRequest.of(1, 25), 75)));
        assertTrue(html.contains("Azul &lt;script&gt;"));
        assertTrue(html.contains("rel=\"noopener noreferrer\""));
        assertTrue(html.contains(game.bggLink()));
        assertTrue(html.contains("Sold"));
        assertTrue(html.contains("Previous"));
        assertTrue(html.contains("Next"));
        assertTrue(html.contains("Page 2 of 3"));
    }

    @Test
    void rendersGameCreationAndEditingFields() {
        var input = new BoardGameInput("Azul", game.bggLink(), game.state(), game.type());
        var html = render("game-form", Map.of("form", input, "gameId", "game"));
        assertTrue(html.contains("name=\"name\""));
        assertTrue(html.contains("value=\"Azul\""));
        assertTrue(html.contains("name=\"bggLink\""));
        assertTrue(html.contains("value=\"SOLD\" selected=\"selected\""));
        assertTrue(html.contains("name=\"type\""));
        assertTrue(render("game-form", Map.of("form", input)).contains("Save game"));
    }

    @Test
    void rendersPlayFormWithSelectedGameAndRatingConstraints() {
        var input = new BoardGamePlayInput("game", LocalDateTime.of(2026, 11, 10, 18, 30), 5);
        var html = render("play-form", Map.of("form", input, "games", List.of(game)));
        assertTrue(html.contains("value=\"game\" selected=\"selected\""));
        assertTrue(html.contains("type=\"datetime-local\""));
        assertTrue(html.contains("min=\"1\" max=\"5\" step=\"1\""));
        assertTrue(html.contains("name=\"rating\""));
    }

    @Test
    void promptsToCreateGameBeforeRecordingFirstPlay() {
        var html = render("play-form", Map.of("form", new BoardGamePlayInput(null, null, null), "games", List.of()));
        assertTrue(html.contains("Add a board game before recording a play."));
        assertFalse(html.contains("name=\"rating\""));
    }

    @Test
    void rendersPlayHistoryAndSearchFields() {
        var time = LocalDateTime.of(2026, 11, 10, 18, 30);
        var play = new BoardGamePlayDto("play", "game", "Azul", time, 4);
        var html = render(
                "plays",
                Map.of(
                        "filter",
                        new BoardGamePlaySearchFilter("Azul", time.minusDays(1), time, 1, 5),
                        "results",
                        new PageImpl<>(List.of(play))));
        assertTrue(html.contains("2026-11-10 18:30"));
        assertTrue(html.contains("4 / 5"));
        assertTrue(html.contains("name=\"ratingMin\""));
        assertTrue(html.contains("name=\"from\""));
    }

    @Test
    void rendersEmptyListsAndValidationFeedback() {
        var html = render(
                "games",
                Map.of(
                        "filter",
                        new BoardGameSearchFilter(null, null, null),
                        "results",
                        new PageImpl<>(List.of()),
                        "error",
                        "Invalid search"));
        assertTrue(html.contains("No games found."));
        assertTrue(html.contains("Invalid search"));
        assertFalse(html.contains("<table>"));
        html = render(
                "plays",
                Map.of(
                        "filter",
                        new BoardGamePlaySearchFilter(null, null, null, null, null),
                        "results",
                        new PageImpl<>(List.of())));
        assertTrue(html.contains("No plays found."));
    }

    private String render(String template, Map<String, Object> variables) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/boardgame/");
        resolver.setSuffix(".html");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var links = mock(ILinkBuilder.class);
        when(links.getName()).thenReturn("test-links");
        when(links.buildLink(any(), anyString(), any())).thenAnswer(call -> call.getArgument(1));
        engine.setLinkBuilder(links);
        var context = new Context();
        context.setVariables(variables);
        context.setVariable("states", BoardGameState.values());
        context.setVariable("types", BoardGameType.values());
        return engine.process(template, context);
    }
}
