package trd.home.boardgame.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.service.game.BoardGameCatalogService;
import trd.home.boardgame.service.play.BoardGamePlayService;
import trd.home.common.logging.LogMethodCall;

@Service
@RequiredArgsConstructor
public class BoardGameService {
    private final BoardGameCatalogService catalog;
    private final BoardGamePlayService plays;

    @LogMethodCall
    public BoardGameDto createGame(BoardGameInput input) {
        return catalog.create(input);
    }

    @LogMethodCall
    public BoardGameDto updateGame(String id, BoardGameInput input) {
        return catalog.update(id, input);
    }

    public BoardGameDto getGame(String id) {
        return catalog.get(id);
    }

    public List<BoardGameDto> gameChoices() {
        return catalog.choices();
    }

    public Page<BoardGameDto> searchGames(BoardGameSearchFilter filter, int page) {
        return catalog.search(filter, page);
    }

    @LogMethodCall
    public BoardGamePlayDto recordPlay(BoardGamePlayInput input) {
        return plays.record(input);
    }

    public Page<BoardGamePlayDto> searchPlays(BoardGamePlaySearchFilter filter, int page) {
        return plays.search(filter, page);
    }
}
