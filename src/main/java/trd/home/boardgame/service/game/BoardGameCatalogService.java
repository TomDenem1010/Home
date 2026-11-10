package trd.home.boardgame.service.game;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.boardgame.dao.BoardGame;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.exception.BoardGameNotFoundException;
import trd.home.boardgame.repository.BoardGameRepository;
import trd.home.boardgame.specification.BoardGameSearchSpecification;
import trd.home.boardgame.validator.BoardGameInputValidator;
import trd.home.boardgame.validator.BoardGameSearchValidator;

@Service
@RequiredArgsConstructor
public class BoardGameCatalogService {
    private final BoardGameRepository repository;
    private final List<BoardGameInputValidator> inputValidators;
    private final List<BoardGameSearchValidator> searchValidators;

    @Transactional
    public BoardGameDto create(BoardGameInput input) {
        var normalized = normalized(input);
        inputValidators.forEach(validator -> validator.validate(normalized));
        var game = new BoardGame();
        apply(game, normalized);
        return BoardGameDto.from(repository.save(game));
    }

    @Transactional
    public BoardGameDto update(String id, BoardGameInput input) {
        var normalized = normalized(input);
        inputValidators.forEach(validator -> validator.validate(normalized));
        var game = find(id);
        apply(game, normalized);
        return BoardGameDto.from(repository.save(game));
    }

    @Transactional(readOnly = true)
    public BoardGameDto get(String id) {
        return BoardGameDto.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<BoardGameDto> choices() {
        return repository.findAllByOrderByNameAscIdAsc().stream()
                .map(BoardGameDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<BoardGameDto> search(BoardGameSearchFilter filter, int page) {
        searchValidators.forEach(validator -> validator.validate(filter, page));
        return repository
                .findAll(BoardGameSearchSpecification.matching(filter), PageRequest.of(page, 25, Sort.by("name", "id")))
                .map(BoardGameDto::from);
    }

    private BoardGame find(String id) {
        return repository.findById(id).orElseThrow(() -> new BoardGameNotFoundException(id));
    }

    private BoardGameInput normalized(BoardGameInput input) {
        return new BoardGameInput(
                input.name() == null ? null : input.name().strip(),
                input.bggLink() == null || input.bggLink().isBlank()
                        ? null
                        : input.bggLink().strip(),
                input.state(),
                input.type());
    }

    private void apply(BoardGame game, BoardGameInput input) {
        game.setName(input.name());
        game.setBggLink(input.bggLink());
        game.setState(input.state());
        game.setType(input.type());
    }
}
