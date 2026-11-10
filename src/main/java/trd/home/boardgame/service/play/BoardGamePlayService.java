package trd.home.boardgame.service.play;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.boardgame.dao.BoardGamePlay;
import trd.home.boardgame.dto.BoardGamePlayDto;
import trd.home.boardgame.dto.BoardGamePlayInput;
import trd.home.boardgame.dto.BoardGamePlaySearchFilter;
import trd.home.boardgame.exception.BoardGameNotFoundException;
import trd.home.boardgame.repository.BoardGamePlayRepository;
import trd.home.boardgame.repository.BoardGameRepository;
import trd.home.boardgame.specification.BoardGamePlaySearchSpecification;
import trd.home.boardgame.validator.BoardGamePlayInputValidator;
import trd.home.boardgame.validator.BoardGamePlaySearchValidator;

@Service
@RequiredArgsConstructor
public class BoardGamePlayService {
    private final BoardGamePlayRepository repository;
    private final BoardGameRepository games;
    private final List<BoardGamePlayInputValidator> inputValidators;
    private final List<BoardGamePlaySearchValidator> searchValidators;

    @Transactional
    public BoardGamePlayDto record(BoardGamePlayInput input) {
        inputValidators.forEach(validator -> validator.validate(input));
        var game = games.findById(input.boardGameId())
                .orElseThrow(() -> new BoardGameNotFoundException(input.boardGameId()));
        var play = new BoardGamePlay();
        play.setBoardGame(game);
        play.setTime(input.time());
        play.setRating(input.rating());
        return BoardGamePlayDto.from(repository.save(play));
    }

    @Transactional(readOnly = true)
    public Page<BoardGamePlayDto> search(BoardGamePlaySearchFilter filter, int page) {
        searchValidators.forEach(validator -> validator.validate(filter, page));
        return repository
                .findAll(
                        BoardGamePlaySearchSpecification.matching(filter),
                        PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "time", "id")))
                .map(BoardGamePlayDto::from);
    }
}
