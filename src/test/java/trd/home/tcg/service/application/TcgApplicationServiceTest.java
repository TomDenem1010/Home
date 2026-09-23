package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventType;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketDeckRepository;

class TcgApplicationServiceTest {

    private final ApplicationEventRepository events = mock(ApplicationEventRepository.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);

    @Test
    void createsSelectedDeckSaveEvent() {
        new TcgCommandService(events, notifications).saveDecksFromResource("deck-id");

        InOrder order = inOrder(events, notifications);
        order.verify(events)
                .save(argThat(event ->
                        event.getType() == EventType.SAVE_DECKS_FROM_RESOURCE && "deck-id".equals(event.getMessage())));
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "Deck saving has started.");
    }

    @Test
    void createsPriceRefreshEvent() {
        new TcgCommandService(events, notifications).refreshDeckPrices(null);

        InOrder order = inOrder(events, notifications);
        order.verify(events).save(argThat(event -> event.getType() == EventType.REFRESH_DECK_PRICES));
        order.verify(notifications).publish(FrontendNotificationType.WARNING, "Deck price refresh has started.");
    }

    @Test
    void delegatesPriceQueries() {
        List<CardmarketDeckPriceSummary> summaries = List.of(mock(CardmarketDeckPriceSummary.class));
        CardmarketDeckPriceHistorySummary history = mock(CardmarketDeckPriceHistorySummary.class);
        when(decks.calculateActiveDeckPriceSummaries()).thenReturn(summaries);
        when(decks.calculateDeckPriceHistorySummary("deck-id")).thenReturn(history);
        TcgQueryService service = new TcgQueryService(decks);

        assertSame(summaries, service.getDeckPriceSummary());
        assertSame(history, service.getDeckPriceHistorySummary("deck-id"));
    }
}
