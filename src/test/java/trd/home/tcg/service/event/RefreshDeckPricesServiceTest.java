package trd.home.tcg.service.event;

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
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

class RefreshDeckPricesServiceTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final CardmarketCardPriceSaver cardPriceSaver = mock(CardmarketCardPriceSaver.class);
    private final CardmarketCardRepository cardRepository = mock(CardmarketCardRepository.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final RefreshDeckPricesService service = new RefreshDeckPricesService(
            new ApplicationEventProcessor(eventQueue, notificationPublisher),
            cardPriceSaver,
            cardRepository,
            deckRepository);

    @Test
    void refreshesRequestedDecksSequentially() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES);
        List<CardmarketCard> firstEntities = List.of(card("card-1"));
        List<CardmarketCard> secondEntities = List.of(card("card-2"));
        when(deckRepository.findIdsByStatus(DeckStatus.ACTIVE)).thenReturn(List.of("deck-1", "deck-2"));
        when(cardRepository.findAllInCurrentDeckVersionByDeckId("deck-1")).thenReturn(firstEntities);
        when(cardRepository.findAllInCurrentDeckVersionByDeckId("deck-2")).thenReturn(secondEntities);

        service.process(event);

        InOrder order = inOrder(cardRepository, cardPriceSaver);
        order.verify(cardRepository).findAllInCurrentDeckVersionByDeckId("deck-1");
        order.verify(cardPriceSaver).updateCardPrice(List.of(CardmarketCardDto.from(firstEntities.getFirst())));
        order.verify(cardRepository).findAllInCurrentDeckVersionByDeckId("deck-2");
        order.verify(cardPriceSaver).updateCardPrice(List.of(CardmarketCardDto.from(secondEntities.getFirst())));
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Deck prices were refreshed successfully.");
    }

    @Test
    void refreshesOnlyDeckStoredInEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES, "deck-1");
        List<CardmarketCardDto> cards = List.of();
        when(cardRepository.findAllInCurrentDeckVersionByDeckId("deck-1")).thenReturn(List.of());

        service.process(event);

        verify(cardPriceSaver).updateCardPrice(cards);
        assertEquals(EventStatus.DONE, event.getStatus());
    }

    private static CardmarketCard card(String id) {
        CardmarketCard card = new CardmarketCard();
        card.setId(id);
        card.setLink("https://example.test/" + id);
        card.setFoilType(CardFoilType.NO);
        return card;
    }
}
