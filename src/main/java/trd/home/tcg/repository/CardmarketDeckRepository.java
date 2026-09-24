package trd.home.tcg.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketDeck;

public interface CardmarketDeckRepository extends JpaRepository<CardmarketDeck, String> {

    boolean existsByName(String name);

    Optional<CardmarketDeck> findByName(String name);

    @Override
    @EntityGraph(attributePaths = "currentVersion")
    Optional<CardmarketDeck> findById(String id);

    @EntityGraph(attributePaths = "currentVersion")
    List<CardmarketDeck> findAllByStatusOrderByName(DeckStatus status);
}
