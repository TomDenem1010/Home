package trd.home.boardgame.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import trd.home.boardgame.constant.*;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.service.game.BoardGameCatalogService;
import trd.home.boardgame.service.play.BoardGamePlayService;

class BoardGameServiceTest {
    @Test
    void delegatesAllModuleOperationsToOwningServices() {
        var catalog = mock(BoardGameCatalogService.class);
        var plays = mock(BoardGamePlayService.class);
        var service = new BoardGameService(catalog, plays);
        var input = new BoardGameInput("Azul", null, BoardGameState.OWN, BoardGameType.BASE);
        var game = new BoardGameDto("game", input.name(), input.bggLink(), input.state(), input.type());
        var choices = List.of(game);
        var gameFilter = new BoardGameSearchFilter("Azul", null, null);
        var gamePage = new PageImpl<>(choices);
        var playInput = new BoardGamePlayInput("game", LocalDateTime.of(2026, 11, 10, 18, 0), 4);
        var play = new BoardGamePlayDto("play", "game", "Azul", playInput.time(), 4);
        var playFilter = new BoardGamePlaySearchFilter("Azul", null, null, 4, 5);
        var playPage = new PageImpl<>(List.of(play));
        when(catalog.create(input)).thenReturn(game);
        when(catalog.update("game", input)).thenReturn(game);
        when(catalog.get("game")).thenReturn(game);
        when(catalog.choices()).thenReturn(choices);
        when(catalog.search(gameFilter, 1)).thenReturn(gamePage);
        when(plays.record(playInput)).thenReturn(play);
        when(plays.search(playFilter, 2)).thenReturn(playPage);

        assertSame(game, service.createGame(input));
        assertSame(game, service.updateGame("game", input));
        assertSame(game, service.getGame("game"));
        assertSame(choices, service.gameChoices());
        assertSame(gamePage, service.searchGames(gameFilter, 1));
        assertSame(play, service.recordPlay(playInput));
        assertSame(playPage, service.searchPlays(playFilter, 2));
    }
}
