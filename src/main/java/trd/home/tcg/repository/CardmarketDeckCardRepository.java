package trd.home.tcg.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.dao.CardmarketDeckCard;

public interface CardmarketDeckCardRepository extends JpaRepository<CardmarketDeckCard, String> {

    List<CardmarketDeckCard> findAllByDeckVersionIdIn(Collection<String> deckVersionIds);

    List<CardmarketDeckCard> findAllByDeckVersionId(String deckVersionId);
}
