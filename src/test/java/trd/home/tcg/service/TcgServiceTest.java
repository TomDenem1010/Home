package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

class TcgServiceTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final TcgService service = new TcgService(eventRepository, deckRepository, notificationPublisher);

    @Test
    void createsSaveDecksFromResourceEvent() {
        assertDoesNotThrow(service::saveDecksFromResource);

        InOrder order = inOrder(eventRepository, notificationPublisher);
        order.verify(eventRepository).save(argThat(event -> event.getType() == EventType.SAVE_DECKS_FROM_RESOURCE));
        order.verify(notificationPublisher).publish(FrontendNotificationType.WARNING, "Deck saving has started.");
    }

    @Test
    void createsRefreshDeckPricesEvent() {
        assertDoesNotThrow(service::refreshDeckPrices);

        InOrder order = inOrder(eventRepository, notificationPublisher);
        order.verify(eventRepository).save(argThat(event -> event.getType() == EventType.REFRESH_DECK_PRICES));
        order.verify(notificationPublisher)
                .publish(FrontendNotificationType.WARNING, "Deck price refresh has started.");
    }

    @Test
    void returnsDeckPriceSummaries() {
        List<CardmarketDeckPriceSummary> summaries = List.of(mock(CardmarketDeckPriceSummary.class));
        when(deckRepository.calculateActiveDeckPriceSummaries()).thenReturn(summaries);

        assertSame(summaries, service.getDeckPriceSummary());
    }

    @Test
    void returnsDeckPriceHistorySummary() {
        CardmarketDeckPriceHistorySummary summary = mock(CardmarketDeckPriceHistorySummary.class);
        when(deckRepository.calculateDeckPriceHistorySummary("deck-id")).thenReturn(summary);

        assertSame(summary, service.getDeckPriceHistorySummary("deck-id"));
    }
}
