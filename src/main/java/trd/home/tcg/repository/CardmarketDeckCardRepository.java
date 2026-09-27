package trd.home.tcg.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import trd.home.tcg.dao.CardmarketDeckCard;

public interface CardmarketDeckCardRepository
        extends JpaRepository<CardmarketDeckCard, String>, JpaSpecificationExecutor<CardmarketDeckCard> {

    @Override
    @EntityGraph(attributePaths = {"deckVersion", "card"})
    List<CardmarketDeckCard> findAll(Specification<CardmarketDeckCard> specification);

    @EntityGraph(attributePaths = {"deckVersion", "card"})
    List<CardmarketDeckCard> findAllByDeckVersionIdIn(Collection<String> deckVersionIds);

    @EntityGraph(attributePaths = "card")
    List<CardmarketDeckCard> findAllByDeckVersionId(String deckVersionId);
}
