package trd.home.tcg.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dto.CardmarketDeckCardPriceSummary;
import trd.home.tcg.dto.CardmarketDeckDto;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;

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

    default List<CardmarketDeckPriceSummary> calculateActiveDeckPriceSummaries() {
        return calculateActiveDeckPriceSummaryProjections().stream()
                .map(projection -> new CardmarketDeckPriceSummary(
                        Objects.requireNonNullElse(projection.getDeckId(), ""),
                        Objects.requireNonNullElse(projection.getDeckName(), ""),
                        Objects.requireNonNullElse(projection.getSumFromInEuro(), BigDecimal.ZERO),
                        Objects.requireNonNullElse(projection.getSumTrendInEuro(), BigDecimal.ZERO)))
                .toList();
    }

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

    default CardmarketDeckPriceHistorySummary calculateDeckPriceHistorySummary(String deckId) {
        List<CardmarketDeckCardPriceSummary> cards = calculateDeckCardPriceSummaryProjections(deckId).stream()
                .map(projection -> new CardmarketDeckCardPriceSummary(
                        Objects.requireNonNullElse(projection.getCardName(), ""),
                        Objects.requireNonNullElse(projection.getCardLink(), ""),
                        projection.getQuantity(),
                        Objects.requireNonNullElse(projection.getLatestFromInEuro(), BigDecimal.ZERO),
                        Objects.requireNonNullElse(projection.getLatestTrendInEuro(), BigDecimal.ZERO),
                        Objects.requireNonNullElse(projection.getLatestPriceCreatedAt(), Instant.EPOCH)))
                .toList();

        BigDecimal totalFrom = totalPrice(cards, CardmarketDeckCardPriceSummary::latestFromInEuro);
        BigDecimal totalTrend = totalPrice(cards, CardmarketDeckCardPriceSummary::latestTrendInEuro);
        return new CardmarketDeckPriceHistorySummary(
                Objects.requireNonNullElse(deckId, ""), cards, totalFrom, totalTrend);
    }

    private static BigDecimal totalPrice(
            List<CardmarketDeckCardPriceSummary> cards,
            Function<CardmarketDeckCardPriceSummary, BigDecimal> priceExtractor) {
        return cards.stream()
                .map(card -> priceExtractor.apply(card).multiply(BigDecimal.valueOf(card.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        String getCardLink();

        int getQuantity();

        BigDecimal getLatestFromInEuro();

        BigDecimal getLatestTrendInEuro();

        Instant getLatestPriceCreatedAt();
    }
}
