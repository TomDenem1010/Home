package trd.home.boardgame.service.play;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.dao.*;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.exception.*;
import trd.home.boardgame.repository.*;
import trd.home.boardgame.validator.*;

class BoardGamePlayServiceTest {
    private final BoardGamePlayRepository repository = mock(BoardGamePlayRepository.class);
    private final BoardGameRepository games = mock(BoardGameRepository.class);
    private final BoardGamePlayInputValidator validator = mock(BoardGamePlayInputValidator.class);
    private final BoardGamePlaySearchValidator searchValidator = mock(BoardGamePlaySearchValidator.class);
    private final BoardGamePlayService service =
            new BoardGamePlayService(repository, games, List.of(validator), List.of(searchValidator));
    private final BoardGamePlayInput input = new BoardGamePlayInput("game", LocalDateTime.of(2026, 11, 10, 18, 30), 5);

    @Test
    void recordsPlayForExistingGameEvenWhenSold() {
        var game = game();
        game.setState(BoardGameState.SOLD);
        when(games.findById("game")).thenReturn(Optional.of(game));
        when(repository.save(any())).thenAnswer(invocation -> {
            BoardGamePlay play = invocation.getArgument(0);
            play.setId("play");
            return play;
        });
        assertEquals(new BoardGamePlayDto("play", "game", "Azul", input.time(), 5), service.record(input));
        var order = inOrder(validator, games, repository);
        order.verify(validator).validate(input);
        order.verify(games).findById("game");
        order.verify(repository).save(any(BoardGamePlay.class));
    }

    @Test
    void rejectsInvalidInputBeforeAnyDatabaseAccess() {
        doThrow(new InvalidBoardGameException("Invalid rating")).when(validator).validate(input);
        assertThrows(InvalidBoardGameException.class, () -> service.record(input));
        verifyNoInteractions(repository, games);
    }

    @Test
    void rejectsUnknownGameWithoutSavingPlay() {
        when(games.findById("game")).thenReturn(Optional.empty());
        assertThrows(BoardGameNotFoundException.class, () -> service.record(input));
        verifyNoInteractions(repository);
    }

    @Test
    void mapsAndPagesPlaysWithMostRecentFirst() {
        var play = new BoardGamePlay();
        play.setId("play");
        play.setBoardGame(game());
        play.setTime(input.time());
        play.setRating(5);
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<BoardGamePlay>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(play)));
        var filter = new BoardGamePlaySearchFilter("Azul", null, null, 4, 5);
        assertEquals(
                List.of(BoardGamePlayDto.from(play)), service.search(filter, 1).getContent());
        var pageable = ArgumentCaptor.forClass(Pageable.class);
        var order = inOrder(searchValidator, repository);
        order.verify(searchValidator).validate(filter, 1);
        order.verify(repository)
                .findAll(org.mockito.ArgumentMatchers.<Specification<BoardGamePlay>>any(), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(25, pageable.getValue().getPageSize());
        assertEquals(
                Sort.by(Sort.Direction.DESC, "time", "id"), pageable.getValue().getSort());
    }

    @Test
    void rejectsInvalidFiltersBeforeQuerying() {
        var filter = new BoardGamePlaySearchFilter(null, null, null, 5, 1);
        doThrow(new InvalidBoardGameException("range")).when(searchValidator).validate(filter, 0);
        assertThrows(InvalidBoardGameException.class, () -> service.search(filter, 0));
        verifyNoInteractions(repository, games);
    }

    private BoardGame game() {
        var game = new BoardGame();
        game.setId("game");
        game.setName("Azul");
        return game;
    }
}
