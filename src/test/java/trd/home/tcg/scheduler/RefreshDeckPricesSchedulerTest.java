package trd.home.tcg.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

class RefreshDeckPricesSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketCardPriceSaver cardPriceSaver = mock(CardmarketCardPriceSaver.class);
    private final CardmarketCardRepository cardRepository = mock(CardmarketCardRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final RefreshDeckPricesScheduler scheduler =
            new RefreshDeckPricesScheduler(eventRepository, cardPriceSaver, cardRepository, notificationPublisher);

    @Test
    void processesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCardDto> cards = List.of();
        List<EventStatus> savedStatuses = new ArrayList<>();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.REFRESH_DECK_PRICES, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(cardRepository.findAllInActiveDeckCurrentVersions()).thenReturn(cards);
        doAnswer(invocation -> {
                    savedStatuses.add(event.getStatus());
                    return event;
                })
                .when(eventRepository)
                .save(event);

        scheduler.processNextEvent();

        InOrder order = inOrder(eventRepository, cardPriceSaver);
        order.verify(eventRepository).save(event);
        order.verify(cardPriceSaver).updateCardPrice(cards);
        order.verify(eventRepository).save(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        assertEquals(List.of(EventStatus.PROCESSING, EventStatus.DONE), savedStatuses);
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Deck prices were refreshed successfully.");
    }

    @Test
    void storesErrorWhenProcessingFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCardDto> cards = List.of();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.REFRESH_DECK_PRICES, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(cardRepository.findAllInActiveDeckCurrentVersions()).thenReturn(cards);
        doThrow(new IllegalStateException("Unable to refresh prices"))
                .when(cardPriceSaver)
                .updateCardPrice(cards);

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("Unable to refresh prices", event.getErrorMessage());
        verify(notificationPublisher)
                .publish(
                        null,
                        FrontendNotificationType.ERROR,
                        "Failed to refresh deck prices: Unable to refresh prices");
        InOrder order = inOrder(eventRepository, cardPriceSaver);
        order.verify(eventRepository).save(event);
        order.verify(cardPriceSaver).updateCardPrice(cards);
        order.verify(eventRepository).save(event);
    }

    @Test
    void keepsCompletedWorkDoneWhenNotificationPublishingFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCardDto> cards = List.of();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.REFRESH_DECK_PRICES, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(cardRepository.findAllInActiveDeckCurrentVersions()).thenReturn(cards);
        doThrow(new IllegalStateException("Database unavailable"))
                .when(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Deck prices were refreshed successfully.");

        assertThrows(IllegalStateException.class, scheduler::processNextEvent);

        assertEquals(EventStatus.DONE, event.getStatus());
        InOrder order = inOrder(eventRepository, cardPriceSaver, notificationPublisher);
        order.verify(eventRepository).save(event);
        order.verify(cardPriceSaver).updateCardPrice(cards);
        order.verify(eventRepository).save(event);
        order.verify(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Deck prices were refreshed successfully.");
    }
}
