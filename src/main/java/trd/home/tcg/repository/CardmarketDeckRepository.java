package trd.home.tcg.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketDeck;

public interface CardmarketDeckRepository extends JpaRepository<CardmarketDeck, String> {

    boolean existsByName(String name);

    Optional<CardmarketDeck> findByName(String name);

    List<CardmarketDeck> findAllByStatusOrderByName(DeckStatus status);
}
