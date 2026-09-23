package trd.home.tcg.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketDeck;

public interface CardmarketDeckRepository extends JpaRepository<CardmarketDeck, String> {

    boolean existsByName(String name);

    Optional<CardmarketDeck> findByName(String name);

    @Query("SELECT deck.id FROM CardmarketDeck deck WHERE deck.status = :status")
    List<String> findIdsByStatus(@Param("status") DeckStatus status);

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
                deck.id AS "deckId",
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

    @Query(value = """
            WITH latest_card_prices AS (
                SELECT
                    price.card_id,
                    price.from_in_euro,
                    price.trend_in_euro,
                    price.created_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY price.card_id
                        ORDER BY price.created_at DESC, price.id DESC
                    ) AS price_order
                FROM cardmarket_card_price price
            )
            SELECT
                deck.id AS "deckId",
                REGEXP_SUBSTR(
                    SUBSTR(card.link, 1, INSTR(card.link || '?', '?') - 1),
                    '[^/]+$'
                ) AS "cardName",
                card.link AS "cardLink",
                deck_card.quantity AS "quantity",
                latest_price.from_in_euro AS "latestFromInEuro",
                latest_price.trend_in_euro AS "latestTrendInEuro",
                latest_price.created_at AS "latestPriceCreatedAt"
            FROM cardmarket_deck deck
            JOIN cardmarket_deck_version current_version
                ON current_version.id = deck.current_version_id
            JOIN cardmarket_deck_version_card deck_card
                ON deck_card.deck_version_id = current_version.id
            JOIN cardmarket_card card
                ON card.id = deck_card.card_id
            LEFT JOIN latest_card_prices latest_price
                ON latest_price.card_id = deck_card.card_id
                AND latest_price.price_order = 1
            WHERE deck.id = :deckId
            ORDER BY "cardName"
            """, nativeQuery = true)
    List<CardmarketDeckCardPriceProjection> calculateDeckCardPriceSummaryProjections(@Param("deckId") String deckId);

    interface CardmarketDeckPriceProjection {

        String getDeckId();

        String getDeckName();

        java.math.BigDecimal getSumFromInEuro();

        java.math.BigDecimal getSumTrendInEuro();
    }

    interface CardmarketDeckCardPriceProjection {

        String getDeckId();

        String getCardName();

        String getCardLink();

        int getQuantity();

        java.math.BigDecimal getLatestFromInEuro();

        java.math.BigDecimal getLatestTrendInEuro();

        Instant getLatestPriceCreatedAt();
    }
}
