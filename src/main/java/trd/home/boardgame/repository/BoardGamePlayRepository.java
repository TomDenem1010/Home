package trd.home.boardgame.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import trd.home.boardgame.dao.BoardGamePlay;

public interface BoardGamePlayRepository
        extends JpaRepository<BoardGamePlay, String>, JpaSpecificationExecutor<BoardGamePlay> {
    @Override
    @EntityGraph(attributePaths = "boardGame")
    Page<BoardGamePlay> findAll(Specification<BoardGamePlay> specification, Pageable pageable);
}
