package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

class RefreshDeckPricesServiceTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketCardPriceSaver cardPriceSaver = mock(CardmarketCardPriceSaver.class);
    private final CardmarketCardRepository cardRepository = mock(CardmarketCardRepository.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final RefreshDeckPricesService service = new RefreshDeckPricesService(
            new TcgEventProcessor(eventRepository, notificationPublisher),
            cardPriceSaver,
            cardRepository,
            deckRepository);

    @Test
    void refreshesRequestedDecksSequentially() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCardDto> firstCards = List.of(mock(CardmarketCardDto.class));
        List<CardmarketCardDto> secondCards = List.of(mock(CardmarketCardDto.class));
        when(deckRepository.findIdsByStatus(DeckStatus.ACTIVE)).thenReturn(List.of("deck-1", "deck-2"));
        when(cardRepository.findAllInDeckCurrentVersion("deck-1")).thenReturn(firstCards);
        when(cardRepository.findAllInDeckCurrentVersion("deck-2")).thenReturn(secondCards);

        service.process(event);

        InOrder order = inOrder(cardRepository, cardPriceSaver);
        order.verify(cardRepository).findAllInDeckCurrentVersion("deck-1");
        order.verify(cardPriceSaver).updateCardPrice(firstCards);
        order.verify(cardRepository).findAllInDeckCurrentVersion("deck-2");
        order.verify(cardPriceSaver).updateCardPrice(secondCards);
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Deck prices were refreshed successfully.");
    }

    @Test
    void refreshesOnlyDeckStoredInEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES, "deck-1");
        List<CardmarketCardDto> cards = List.of();
        when(cardRepository.findAllInDeckCurrentVersion("deck-1")).thenReturn(cards);

        service.process(event);

        verify(cardPriceSaver).updateCardPrice(cards);
        assertEquals(EventStatus.DONE, event.getStatus());
    }
}
