package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketDeckRepository;

class TcgApplicationServiceTest {

    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);

    @Test
    void createsSelectedDeckSaveEvent() {
        new TcgCommandService(events, notifications).saveDecksFromResource("deck-id");

        InOrder order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.SAVE_DECKS_FROM_RESOURCE, "deck-id");
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "Deck saving has started.");
    }

    @Test
    void createsPriceRefreshEvent() {
        new TcgCommandService(events, notifications).refreshDeckPrices(null);

        InOrder order = inOrder(events, notifications);
        order.verify(events).enqueue(EventType.REFRESH_DECK_PRICES, null);
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "Deck price refresh has started.");
    }

    @Test
    void mapsPriceSummaryProjections() {
        var projection = mock(CardmarketDeckRepository.CardmarketDeckPriceProjection.class);
        when(projection.getDeckId()).thenReturn("deck-id");
        when(projection.getDeckName()).thenReturn("Deck");
        when(projection.getSumFromInEuro()).thenReturn(new BigDecimal("10.50"));
        when(projection.getSumTrendInEuro()).thenReturn(new BigDecimal("11.50"));
        when(decks.calculateActiveDeckPriceSummaryProjections()).thenReturn(List.of(projection));
        TcgQueryService service = new TcgQueryService(decks);

        List<CardmarketDeckPriceSummary> result = service.getDeckPriceSummary();

        assertEquals(1, result.size());
        assertEquals("deck-id", result.getFirst().deckId());
        assertEquals(new BigDecimal("10.50"), result.getFirst().sumFromInEuro());
    }

    @Test
    void mapsHistoryAndCalculatesTotalsWithUnpricedCards() {
        var priced = mock(CardmarketDeckRepository.CardmarketDeckCardPriceProjection.class);
        var unpriced = mock(CardmarketDeckRepository.CardmarketDeckCardPriceProjection.class);
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(priced.getCardName()).thenReturn("Card");
        when(priced.getCardLink()).thenReturn("https://example.test/card");
        when(priced.getQuantity()).thenReturn(2);
        when(priced.getLatestFromInEuro()).thenReturn(new BigDecimal("3.00"));
        when(priced.getLatestTrendInEuro()).thenReturn(new BigDecimal("4.00"));
        when(priced.getLatestPriceCreatedAt()).thenReturn(createdAt);
        when(unpriced.getCardName()).thenReturn("Unpriced");
        when(unpriced.getQuantity()).thenReturn(100);
        when(decks.calculateDeckCardPriceSummaryProjections("deck-id")).thenReturn(List.of(priced, unpriced));

        CardmarketDeckPriceHistorySummary result = new TcgQueryService(decks).getDeckPriceHistorySummary("deck-id");

        assertEquals("deck-id", result.deckId());
        assertEquals(2, result.cards().size());
        assertEquals(Instant.EPOCH, result.cards().getLast().latestPriceCreatedAt());
        assertEquals(new BigDecimal("6.00"), result.sumLatestFromInEuro());
        assertEquals(new BigDecimal("8.00"), result.sumLatestTrendInEuro());
    }
}
