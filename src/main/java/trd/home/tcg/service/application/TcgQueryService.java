package trd.home.tcg.service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.tcg.dto.CardmarketDeckCardPriceSummary;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@RequiredArgsConstructor
public class TcgQueryService {

    private final CardmarketDeckRepository deckRepository;

    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        return deckRepository.calculateActiveDeckPriceSummaryProjections().stream()
                .map(projection -> new CardmarketDeckPriceSummary(
                        Objects.requireNonNullElse(projection.getDeckId(), ""),
                        Objects.requireNonNullElse(projection.getDeckName(), ""),
                        Objects.requireNonNullElse(projection.getSumFromInEuro(), BigDecimal.ZERO),
                        Objects.requireNonNullElse(projection.getSumTrendInEuro(), BigDecimal.ZERO)))
                .toList();
    }

    public CardmarketDeckPriceHistorySummary getDeckPriceHistorySummary(String deckId) {
        List<CardmarketDeckCardPriceSummary> cards =
                deckRepository.calculateDeckCardPriceSummaryProjections(deckId).stream()
                        .map(projection -> new CardmarketDeckCardPriceSummary(
                                Objects.requireNonNullElse(projection.getCardName(), ""),
                                Objects.requireNonNullElse(projection.getCardLink(), ""),
                                projection.getQuantity(),
                                Objects.requireNonNullElse(projection.getLatestFromInEuro(), BigDecimal.ZERO),
                                Objects.requireNonNullElse(projection.getLatestTrendInEuro(), BigDecimal.ZERO),
                                Objects.requireNonNullElse(projection.getLatestPriceCreatedAt(), Instant.EPOCH)))
                        .toList();
        return new CardmarketDeckPriceHistorySummary(
                Objects.requireNonNullElse(deckId, ""),
                cards,
                totalPrice(cards, CardmarketDeckCardPriceSummary::latestFromInEuro),
                totalPrice(cards, CardmarketDeckCardPriceSummary::latestTrendInEuro));
    }

    private static BigDecimal totalPrice(
            List<CardmarketDeckCardPriceSummary> cards,
            Function<CardmarketDeckCardPriceSummary, BigDecimal> priceExtractor) {
        return cards.stream()
                .map(card -> priceExtractor.apply(card).multiply(BigDecimal.valueOf(card.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
