package trd.home.tcg.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.dao.CardmarketDeckCard;

public interface CardmarketDeckCardRepository extends JpaRepository<CardmarketDeckCard, String> {

    @EntityGraph(attributePaths = {"deckVersion", "card"})
    List<CardmarketDeckCard> findAllByDeckVersionIdIn(Collection<String> deckVersionIds);

    @EntityGraph(attributePaths = "card")
    List<CardmarketDeckCard> findAllByDeckVersionId(String deckVersionId);
}
