package trd.home.boardgame.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import trd.home.boardgame.dao.BoardGame;

public interface BoardGameRepository extends JpaRepository<BoardGame, String>, JpaSpecificationExecutor<BoardGame> {
    List<BoardGame> findAllByOrderByNameAscIdAsc();
}
