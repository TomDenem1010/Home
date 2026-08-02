package trd.home.tcg.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dto.CardmarketCardDto;

public interface CardmarketCardRepository extends JpaRepository<CardmarketCard, String> {

    Optional<CardmarketCard> findByLinkAndFoilType(String link, CardFoilType foilType);

    @Query("""
            SELECT DISTINCT card
            FROM CardmarketDeck deck
            JOIN deck.currentVersion deckVersion
            JOIN deckVersion.cards deckCard
            JOIN deckCard.card card
            WHERE deck.status = :status
            """)
    List<CardmarketCard> findAllInCurrentDeckVersionsByStatus(@Param("status") DeckStatus status);

    default List<CardmarketCardDto> findAllInActiveDeckCurrentVersions() {
        return findAllInCurrentDeckVersionsByStatus(DeckStatus.ACTIVE).stream()
                .map(CardmarketCardDto::from)
                .toList();
    }

    default Optional<CardmarketCardDto> findByUuid(String uuid) {
        return findById(uuid).map(CardmarketCardDto::from);
    }
}
