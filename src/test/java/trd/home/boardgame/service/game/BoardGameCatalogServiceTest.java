package trd.home.boardgame.service.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import trd.home.boardgame.constant.*;
import trd.home.boardgame.dao.BoardGame;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.exception.*;
import trd.home.boardgame.repository.BoardGameRepository;
import trd.home.boardgame.validator.*;

class BoardGameCatalogServiceTest {
    private final BoardGameRepository repository = mock(BoardGameRepository.class);
    private final BoardGameInputValidator first = mock(BoardGameInputValidator.class);
    private final BoardGameInputValidator second = mock(BoardGameInputValidator.class);
    private final BoardGameSearchValidator searchValidator = mock(BoardGameSearchValidator.class);
    private final BoardGameCatalogService service =
            new BoardGameCatalogService(repository, List.of(first, second), List.of(searchValidator));

    @Test
    void normalizesAndValidatesBeforeCreatingGame() {
        when(repository.save(any())).thenAnswer(invocation -> {
            BoardGame game = invocation.getArgument(0);
            game.setId("id");
            return game;
        });
        var input = new BoardGameInput(
                "  Azul  ",
                "  https://boardgamegeek.com/boardgame/230802/azul  ",
                BoardGameState.OWN,
                BoardGameType.BASE);
        var normalized = new BoardGameInput(
                "Azul", "https://boardgamegeek.com/boardgame/230802/azul", BoardGameState.OWN, BoardGameType.BASE);
        var result = service.create(input);
        assertEquals(
                new BoardGameDto("id", normalized.name(), normalized.bggLink(), normalized.state(), normalized.type()),
                result);
        var order = inOrder(first, second, repository);
        order.verify(first).validate(normalized);
        order.verify(second).validate(normalized);
        order.verify(repository).save(any(BoardGame.class));
    }

    @Test
    void stopsWithoutPersistenceWhenValidationFails() {
        var input = new BoardGameInput(null, null, null, null);
        doThrow(new InvalidBoardGameException("Invalid")).when(first).validate(input);
        assertThrows(InvalidBoardGameException.class, () -> service.create(input));
        verifyNoInteractions(repository, second);
    }

    @Test
    void updatesExistingGameIncludingSoldStateAndOptionalLink() {
        var game = game();
        when(repository.findById("id")).thenReturn(Optional.of(game));
        when(repository.save(game)).thenReturn(game);
        var result = service.update(
                "id", new BoardGameInput(" New name ", " ", BoardGameState.SOLD, BoardGameType.BASE_AND_EXPANSION));
        assertEquals("id", result.id());
        assertEquals("New name", result.name());
        assertEquals(BoardGameState.SOLD, result.state());
        assertEquals(BoardGameType.BASE_AND_EXPANSION, result.type());
        assertNull(result.bggLink());
    }

    @Test
    void rejectsMissingGameOnReadAndUpdate() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(BoardGameNotFoundException.class, () -> service.get("missing"));
        assertThrows(
                BoardGameNotFoundException.class,
                () -> service.update(
                        "missing", new BoardGameInput("Game", null, BoardGameState.OWN, BoardGameType.BASE)));
        verify(repository, never()).save(any());
    }

    @Test
    void mapsGameAndChoiceResultsWithoutExposingEntities() {
        var game = game();
        when(repository.findById("id")).thenReturn(Optional.of(game));
        when(repository.findAllByOrderByNameAscIdAsc()).thenReturn(List.of(game));
        assertEquals(BoardGameDto.from(game), service.get("id"));
        assertEquals(List.of(BoardGameDto.from(game)), service.choices());
    }

    @Test
    void validatesSearchAndUsesStableDatabasePaging() {
        var game = game();
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<BoardGame>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));
        var filter = new BoardGameSearchFilter("Azul", BoardGameState.OWN, BoardGameType.BASE);
        var result = service.search(filter, 2);
        assertEquals(List.of(BoardGameDto.from(game)), result.getContent());
        var pageable = ArgumentCaptor.forClass(Pageable.class);
        var order = inOrder(searchValidator, repository);
        order.verify(searchValidator).validate(filter, 2);
        order.verify(repository)
                .findAll(org.mockito.ArgumentMatchers.<Specification<BoardGame>>any(), pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(25, pageable.getValue().getPageSize());
        assertEquals(
                List.of("name", "id"),
                pageable.getValue().getSort().stream()
                        .map(value -> value.getProperty())
                        .toList());
    }

    @Test
    void rejectsInvalidSearchBeforeQuerying() {
        var filter = new BoardGameSearchFilter(null, null, null);
        doThrow(new InvalidBoardGameException("page")).when(searchValidator).validate(filter, -1);
        assertThrows(InvalidBoardGameException.class, () -> service.search(filter, -1));
        verifyNoInteractions(repository);
    }

    private BoardGame game() {
        var game = new BoardGame();
        game.setId("id");
        game.setName("Azul");
        game.setState(BoardGameState.OWN);
        game.setType(BoardGameType.BASE);
        return game;
    }
}
