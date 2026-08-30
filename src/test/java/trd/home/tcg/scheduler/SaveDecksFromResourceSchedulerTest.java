package trd.home.tcg.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.service.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

class SaveDecksFromResourceSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketDeckSaver deckSaver = mock(CardmarketDeckSaver.class);
    private final DeckFileReader deckFileReader = mock(DeckFileReader.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final SaveDecksFromResourceScheduler scheduler =
            new SaveDecksFromResourceScheduler(eventRepository, deckSaver, deckFileReader, notificationPublisher);

    @Test
    void processesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        CardmarketDeck deck = new CardmarketDeck();
        List<EventStatus> savedStatuses = new ArrayList<>();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(deckFileReader.read()).thenReturn(List.of(deck));
        doAnswer(invocation -> {
                    savedStatuses.add(event.getStatus());
                    return event;
                })
                .when(eventRepository)
                .save(event);

        scheduler.processNextEvent();

        InOrder order = inOrder(eventRepository, deckSaver);
        order.verify(eventRepository).save(event);
        order.verify(deckSaver).save(deck);
        order.verify(eventRepository).save(event);
        assertEquals(EventStatus.DONE, event.getStatus());
        assertNotNull(event.getProcessedAt());
        assertEquals(List.of(EventStatus.PROCESSING, EventStatus.DONE), savedStatuses);
        verify(notificationPublisher).publish(null, FrontendNotificationType.SUCCESS, "Decks were saved successfully.");
    }

    @Test
    void storesErrorWhenProcessingFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE);
        CardmarketDeck deck = new CardmarketDeck();
        when(eventRepository.findFirstByTypeAndStatusOrderByCreatedAtAsc(
                        EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO))
                .thenReturn(Optional.of(event));
        when(deckFileReader.read()).thenReturn(List.of(deck));
        doThrow(new IllegalStateException("Unable to save deck"))
                .when(deckSaver)
                .save(deck);

        scheduler.processNextEvent();

        assertEquals(EventStatus.ERROR, event.getStatus());
        assertNull(event.getProcessedAt());
        assertEquals("Unable to save deck", event.getErrorMessage());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.ERROR, "Failed to save decks: Unable to save deck");
        InOrder order = inOrder(eventRepository, deckSaver);
        order.verify(eventRepository).save(event);
        order.verify(deckSaver).save(deck);
        order.verify(eventRepository).save(event);
    }
}
