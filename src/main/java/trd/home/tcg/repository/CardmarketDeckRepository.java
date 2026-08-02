package trd.home.tcg.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dto.CardmarketDeckDto;

public interface CardmarketDeckRepository extends JpaRepository<CardmarketDeck, String> {

    boolean existsByName(String name);

    Optional<CardmarketDeck> findByName(String name);

    @EntityGraph(attributePaths = {"currentVersion", "currentVersion.cards", "currentVersion.cards.card"})
    Optional<CardmarketDeck> findEntityById(String id);

    default Optional<CardmarketDeckDto> findByUuid(String uuid) {
        return findEntityById(uuid).map(CardmarketDeckDto::from);
    }
}
