package trd.home.tcg.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dto.CardmarketDeckDto;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;

public interface CardmarketDeckRepository extends JpaRepository<CardmarketDeck, String> {

    boolean existsByName(String name);

    Optional<CardmarketDeck> findByName(String name);

    @EntityGraph(attributePaths = {"currentVersion", "currentVersion.cards", "currentVersion.cards.card"})
    Optional<CardmarketDeck> findEntityById(String id);

    @Query(value = """
            WITH latest_card_prices AS (
                SELECT
                    price.card_id,
                    price.from_in_euro,
                    price.trend_in_euro,
                    ROW_NUMBER() OVER (
                        PARTITION BY price.card_id
                        ORDER BY price.created_at DESC, price.id DESC
                    ) AS price_order
                FROM cardmarket_card_price price
            )
            SELECT
                deck.name AS "deckName",
                COALESCE(SUM(latest_price.from_in_euro * deck_card.quantity), 0) AS "sumFromInEuro",
                COALESCE(SUM(latest_price.trend_in_euro * deck_card.quantity), 0) AS "sumTrendInEuro"
            FROM cardmarket_deck deck
            LEFT JOIN cardmarket_deck_version current_version
                ON current_version.id = deck.current_version_id
            LEFT JOIN cardmarket_deck_version_card deck_card
                ON deck_card.deck_version_id = current_version.id
            LEFT JOIN latest_card_prices latest_price
                ON latest_price.card_id = deck_card.card_id
                AND latest_price.price_order = 1
            WHERE deck.status = 'ACTIVE'
            GROUP BY deck.id, deck.name
            ORDER BY deck.name
            """, nativeQuery = true)
    List<CardmarketDeckPriceProjection> calculateActiveDeckPriceSummaryProjections();

    default List<CardmarketDeckPriceSummary> calculateActiveDeckPriceSummaries() {
        return calculateActiveDeckPriceSummaryProjections().stream()
                .map(projection -> new CardmarketDeckPriceSummary(
                        projection.getDeckName(), projection.getSumFromInEuro(), projection.getSumTrendInEuro()))
                .toList();
    }

    default Optional<CardmarketDeckDto> findByUuid(String uuid) {
        return findEntityById(uuid).map(CardmarketDeckDto::from);
    }

    interface CardmarketDeckPriceProjection {

        String getDeckName();

        BigDecimal getSumFromInEuro();

        BigDecimal getSumTrendInEuro();
    }
}
