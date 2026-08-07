package trd.home.tcg.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dto.CardmarketDeckCardPriceSummary;
import trd.home.tcg.dto.CardmarketDeckDto;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
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

    default List<CardmarketDeckPriceSummary> calculateActiveDeckPriceSummaries() {
        return calculateActiveDeckPriceSummaryProjections().stream()
                .map(projection -> new CardmarketDeckPriceSummary(
                        projection.getDeckId(),
                        projection.getDeckName(),
                        projection.getSumFromInEuro(),
                        projection.getSumTrendInEuro()))
                .toList();
    }

    @Query(value = """
            WITH ordered_card_prices AS (
                SELECT
                    price.card_id,
                    price.from_in_euro,
                    price.trend_in_euro,
                    price.created_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY price.card_id
                        ORDER BY price.created_at, price.id
                    ) AS first_price_order,
                    ROW_NUMBER() OVER (
                        PARTITION BY price.card_id
                        ORDER BY price.created_at DESC, price.id DESC
                    ) AS latest_price_order
                FROM cardmarket_card_price price
            ),
            first_card_prices AS (
                SELECT card_id, from_in_euro, trend_in_euro, created_at
                FROM ordered_card_prices
                WHERE first_price_order = 1
            ),
            latest_card_prices AS (
                SELECT card_id, from_in_euro, trend_in_euro, created_at
                FROM ordered_card_prices
                WHERE latest_price_order = 1
            )
            SELECT
                deck.id AS "deckId",
                REGEXP_SUBSTR(
                    SUBSTR(card.link, 1, INSTR(card.link || '?', '?') - 1),
                    '[^/]+$'
                ) AS "cardName",
                deck_card.quantity AS "quantity",
                first_price.from_in_euro AS "firstFromInEuro",
                first_price.trend_in_euro AS "firstTrendInEuro",
                first_price.created_at AS "firstPriceCreatedAt",
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
            LEFT JOIN first_card_prices first_price
                ON first_price.card_id = deck_card.card_id
            LEFT JOIN latest_card_prices latest_price
                ON latest_price.card_id = deck_card.card_id
            WHERE deck.id = :deckId
            ORDER BY "cardName"
            """, nativeQuery = true)
    List<CardmarketDeckCardPriceProjection> calculateDeckCardPriceSummaryProjections(@Param("deckId") String deckId);

    default CardmarketDeckPriceHistorySummary calculateDeckPriceHistorySummary(String deckId) {
        List<CardmarketDeckCardPriceSummary> cards = calculateDeckCardPriceSummaryProjections(deckId).stream()
                .map(projection -> new CardmarketDeckCardPriceSummary(
                        projection.getCardName(),
                        projection.getQuantity(),
                        projection.getFirstFromInEuro(),
                        projection.getFirstTrendInEuro(),
                        projection.getFirstPriceCreatedAt(),
                        projection.getLatestFromInEuro(),
                        projection.getLatestTrendInEuro(),
                        projection.getLatestPriceCreatedAt()))
                .toList();

        BigDecimal sumFirstFromInEuro = BigDecimal.ZERO;
        BigDecimal sumFirstTrendInEuro = BigDecimal.ZERO;
        BigDecimal sumLatestFromInEuro = BigDecimal.ZERO;
        BigDecimal sumLatestTrendInEuro = BigDecimal.ZERO;
        for (CardmarketDeckCardPriceSummary card : cards) {
            BigDecimal quantity = BigDecimal.valueOf(card.quantity());
            BigDecimal firstFromInEuro = card.firstFromInEuro();
            BigDecimal firstTrendInEuro = card.firstTrendInEuro();
            BigDecimal latestFromInEuro = card.latestFromInEuro();
            BigDecimal latestTrendInEuro = card.latestTrendInEuro();
            if (firstFromInEuro != null) {
                sumFirstFromInEuro = sumFirstFromInEuro.add(firstFromInEuro.multiply(quantity));
            }
            if (firstTrendInEuro != null) {
                sumFirstTrendInEuro = sumFirstTrendInEuro.add(firstTrendInEuro.multiply(quantity));
            }
            if (latestFromInEuro != null) {
                sumLatestFromInEuro = sumLatestFromInEuro.add(latestFromInEuro.multiply(quantity));
            }
            if (latestTrendInEuro != null) {
                sumLatestTrendInEuro = sumLatestTrendInEuro.add(latestTrendInEuro.multiply(quantity));
            }
        }

        return new CardmarketDeckPriceHistorySummary(
                deckId, cards, sumFirstFromInEuro, sumFirstTrendInEuro, sumLatestFromInEuro, sumLatestTrendInEuro);
    }

    default Optional<CardmarketDeckDto> findByUuid(String uuid) {
        return findEntityById(uuid).map(CardmarketDeckDto::from);
    }

    interface CardmarketDeckPriceProjection {

        String getDeckId();

        String getDeckName();

        BigDecimal getSumFromInEuro();

        BigDecimal getSumTrendInEuro();
    }

    interface CardmarketDeckCardPriceProjection {

        String getDeckId();

        String getCardName();

        int getQuantity();

        BigDecimal getFirstFromInEuro();

        BigDecimal getFirstTrendInEuro();

        Instant getFirstPriceCreatedAt();

        BigDecimal getLatestFromInEuro();

        BigDecimal getLatestTrendInEuro();

        Instant getLatestPriceCreatedAt();
    }
}
