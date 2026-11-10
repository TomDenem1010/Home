package trd.home.frontend.boardgame;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import trd.home.boardgame.constant.*;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.exception.*;
import trd.home.boardgame.service.BoardGameService;
import trd.home.frontend.FrontendPageRenderer;

class BoardGameFrontendControllerTest {
    private final BoardGameService service = mock(BoardGameService.class);
    private final BoardGameFrontendController controller =
            new BoardGameFrontendController(service, new FrontendPageRenderer());
    private final ConcurrentModel model = new ConcurrentModel();
    private final RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
    private final BoardGameInput game = new BoardGameInput("Azul", null, BoardGameState.OWN, BoardGameType.BASE);
    private final BoardGamePlayInput play = new BoardGamePlayInput("game", LocalDateTime.of(2026, 11, 10, 18, 0), 5);

    @Test
    void listsGamesAndTheirSearchResults() {
        var filter = new BoardGameSearchFilter("Azul", BoardGameState.SOLD, BoardGameType.EXPANSION);
        var result = new PageImpl<BoardGameDto>(List.of());
        when(service.searchGames(filter, 2)).thenReturn(result);
        assertEquals("index", controller.games(filter, valid(filter), 2, model));
        assertSame(result, model.getAttribute("results"));
        assertEquals("boardgame/games", model.getAttribute("contentTemplate"));
        assertEquals("redirect:/boardgame/games", controller.index());
        assertArrayEquals(BoardGameState.values(), controller.states());
        assertArrayEquals(BoardGameType.values(), controller.types());
    }

    @Test
    void preservesSearchFormAndShowsEmptyResultsWhenFiltersAreInvalid() {
        var filter = new BoardGameSearchFilter(null, null, null);
        when(service.searchGames(filter, -1)).thenThrow(new InvalidBoardGameException("Invalid page"));
        controller.games(filter, valid(filter), -1, model);
        assertEquals("Invalid page", model.getAttribute("error"));
        assertTrue(((Page<?>) model.getAttribute("results")).isEmpty());
    }

    @Test
    void doesNotSearchWhenEnumBindingFails() {
        var filter = new BoardGameSearchFilter(null, null, null);
        controller.games(filter, invalid(filter), 0, model);
        assertNotNull(model.getAttribute("error"));
        verifyNoInteractions(service);
    }

    @Test
    void rendersCreationAndEditFormsThroughModuleFacade() {
        assertEquals("index", controller.newGame(model));
        assertEquals(BoardGameState.OWN, ((BoardGameInput) model.getAttribute("form")).state());
        assertNull(model.getAttribute("gameId"));
        when(service.getGame("game"))
                .thenReturn(new BoardGameDto("game", "Azul", null, BoardGameState.SOLD, BoardGameType.BASE));
        assertEquals("index", controller.editGame("game", model));
        assertEquals("game", model.getAttribute("gameId"));
        assertEquals(BoardGameState.SOLD, ((BoardGameInput) model.getAttribute("form")).state());
    }

    @Test
    void createsAndUpdatesGamesThenRedirectsWithFeedback() {
        assertEquals("redirect:/boardgame/games", controller.createGame(game, valid(game), model, redirect));
        verify(service).createGame(game);
        assertNotNull(redirect.getFlashAttributes().get("success"));
        assertEquals("redirect:/boardgame/games", controller.updateGame("game", game, valid(game), model, redirect));
        verify(service).updateGame("game", game);
    }

    @Test
    void rerendersInvalidGameWithoutSaving() {
        assertEquals("index", controller.createGame(game, invalid(game), model, redirect));
        assertNotNull(model.getAttribute("error"));
        assertEquals("boardgame/game-form", model.getAttribute("contentTemplate"));
        assertEquals("index", controller.updateGame("game", game, invalid(game), model, redirect));
        assertEquals("game", model.getAttribute("gameId"));
        verifyNoInteractions(service);
    }

    @Test
    void displaysDomainErrorsForCreateAndUpdate() {
        when(service.createGame(game)).thenThrow(new InvalidBoardGameException("Invalid BGG URL"));
        assertEquals("index", controller.createGame(game, valid(game), model, redirect));
        assertEquals("Invalid BGG URL", model.getAttribute("error"));
        when(service.updateGame("missing", game)).thenThrow(new BoardGameNotFoundException("missing"));
        assertEquals("index", controller.updateGame("missing", game, valid(game), model, redirect));
        assertTrue(model.getAttribute("error").toString().contains("missing"));
        assertTrue(redirect.getFlashAttributes().isEmpty());
    }

    @Test
    void rendersPlayChoicesIncludingPreselectedGame() {
        var choices = List.of(new BoardGameDto("game", "Azul", null, BoardGameState.SOLD, BoardGameType.BASE));
        when(service.gameChoices()).thenReturn(choices);
        assertEquals("index", controller.newPlay("game", model));
        assertSame(choices, model.getAttribute("games"));
        assertEquals("game", ((BoardGamePlayInput) model.getAttribute("form")).boardGameId());
        assertEquals("boardgame/play-form", model.getAttribute("contentTemplate"));
    }

    @Test
    void recordsPlayAndRedirects() {
        assertEquals("redirect:/boardgame/plays", controller.recordPlay(play, valid(play), model, redirect));
        verify(service).recordPlay(play);
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void rerendersPlayOnBindingAndDomainFailures() {
        when(service.gameChoices()).thenReturn(List.of());
        assertEquals("index", controller.recordPlay(play, invalid(play), model, redirect));
        verify(service, never()).recordPlay(any());
        when(service.recordPlay(play)).thenThrow(new BoardGameNotFoundException("game"));
        assertEquals("index", controller.recordPlay(play, valid(play), model, redirect));
        assertTrue(model.getAttribute("error").toString().contains("game"));
        assertTrue(redirect.getFlashAttributes().isEmpty());
    }

    @Test
    void listsPlaysAndHandlesInvalidSearches() {
        var filter = new BoardGamePlaySearchFilter("Azul", play.time(), play.time(), 3, 5);
        var results = new PageImpl<BoardGamePlayDto>(List.of());
        when(service.searchPlays(filter, 1)).thenReturn(results);
        assertEquals("index", controller.plays(filter, valid(filter), 1, model));
        assertSame(results, model.getAttribute("results"));
        assertEquals("boardgame/plays", model.getAttribute("contentTemplate"));
        when(service.searchPlays(filter, -1)).thenThrow(new InvalidBoardGameException("Invalid range"));
        assertEquals("index", controller.plays(filter, valid(filter), -1, model));
        assertEquals("Invalid range", model.getAttribute("error"));
    }

    @Test
    void doesNotQueryPlaysOnInvalidDateOrRatingBinding() {
        var filter = new BoardGamePlaySearchFilter(null, null, null, null, null);
        controller.plays(filter, invalid(filter), 0, model);
        verifyNoInteractions(service);
        assertNotNull(model.getAttribute("error"));
    }

    private BindingResult valid(Object target) {
        return new BeanPropertyBindingResult(target, "form");
    }

    private BindingResult invalid(Object target) {
        var binding = valid(target);
        binding.reject("invalid");
        return binding;
    }
}
