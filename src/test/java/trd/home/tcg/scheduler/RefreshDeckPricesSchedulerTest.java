package trd.home.tcg.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

class RefreshDeckPricesSchedulerTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketCardPriceSaver cardPriceSaver = mock(CardmarketCardPriceSaver.class);
    private final CardmarketCardRepository cardRepository = mock(CardmarketCardRepository.class);
    private final RefreshDeckPricesScheduler scheduler =
            new RefreshDeckPricesScheduler(eventRepository, cardPriceSaver, cardRepository);

    @Test
    void processesOldestPendingEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCardDto> cards = List.of();
        when(eventRepository.findFirstByTypeAndProcessedAtIsNullAndErrorMessageIsNullOrderByCreatedAtAsc(
                        EventType.REFRESH_DECK_PRICES))
                .thenReturn(Optional.of(event));
        when(cardRepository.findAllInActiveDeckCurrentVersions()).thenReturn(cards);

        scheduler.processNextEvent();

        verify(cardPriceSaver).updateCardPrice(cards);
        assertNotNull(event.getProcessedAt());
        verify(eventRepository).save(event);
    }

    @Test
    void storesErrorWhenProcessingFails() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCardDto> cards = List.of();
        when(eventRepository.findFirstByTypeAndProcessedAtIsNullAndErrorMessageIsNullOrderByCreatedAtAsc(
                        EventType.REFRESH_DECK_PRICES))
                .thenReturn(Optional.of(event));
        when(cardRepository.findAllInActiveDeckCurrentVersions()).thenReturn(cards);
        doThrow(new IllegalStateException("Unable to refresh prices"))
                .when(cardPriceSaver)
                .updateCardPrice(cards);

        scheduler.processNextEvent();

        assertNull(event.getProcessedAt());
        assertEquals("Unable to refresh prices", event.getErrorMessage());
        verify(eventRepository).save(event);
    }
}
