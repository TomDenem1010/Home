package trd.home.tcg.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

class RefreshDeckPricesServiceTest {

    private final ApplicationEventQueue eventQueue = mock(ApplicationEventQueue.class);
    private final CardmarketCardPriceSaver cardPriceSaver = mock(CardmarketCardPriceSaver.class);
    private final CardmarketCardRepository cardRepository = mock(CardmarketCardRepository.class);
    private final CardmarketDeckCardRepository deckCardRepository = mock(CardmarketDeckCardRepository.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final FrontendNotificationPublisher notificationPublisher = mock(FrontendNotificationPublisher.class);
    private final RefreshDeckPricesService service = new RefreshDeckPricesService(
            new ApplicationEventProcessor(eventQueue, notificationPublisher),
            cardPriceSaver,
            cardRepository,
            deckCardRepository,
            deckRepository);

    @Test
    void refreshesOnlyDeckStoredInEvent() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES, "deck-1");
        CardmarketDeck firstDeck = deck("deck-1", "version-1");
        CardmarketCard firstCard = card("card-1");
        when(deckRepository.findById("deck-1")).thenReturn(Optional.of(firstDeck));
        when(deckCardRepository.findAllByDeckVersionId("version-1"))
                .thenReturn(List.of(deckCard(firstDeck.getCurrentVersion(), firstCard)));
        when(cardRepository.findAllById(List.of("card-1"))).thenReturn(List.of(firstCard));

        service.process(event);

        InOrder order = inOrder(deckCardRepository, cardRepository, cardPriceSaver);
        order.verify(deckCardRepository).findAllByDeckVersionId("version-1");
        order.verify(cardRepository).findAllById(List.of("card-1"));
        order.verify(cardPriceSaver).updateCardPrice(List.of(CardmarketCardDto.from(firstCard)));
        verify(deckRepository, never()).findAllByStatusOrderByName(DeckStatus.ACTIVE);
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(notificationPublisher)
                .publish(null, FrontendNotificationType.SUCCESS, "Deck prices were refreshed successfully.");
    }

    @Test
    void refreshesDeckWithoutCards() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES, "deck-1");
        List<CardmarketCardDto> cards = List.of();
        CardmarketDeck deck = deck("deck-1", "version-1");
        when(deckRepository.findById("deck-1")).thenReturn(Optional.of(deck));
        when(deckCardRepository.findAllByDeckVersionId("version-1")).thenReturn(List.of());
        when(cardRepository.findAllById(List.of())).thenReturn(List.of());

        service.process(event);

        verify(cardPriceSaver).updateCardPrice(cards);
        assertEquals(EventStatus.DONE, event.getStatus());
    }

    @Test
    void marksEventAsErrorWhenDeckDoesNotExist() {
        ApplicationEvent event = new ApplicationEvent(EventType.REFRESH_DECK_PRICES, "missing-deck");

        service.process(event);

        assertEquals(EventStatus.ERROR, event.getStatus());
        verifyNoInteractions(cardPriceSaver);
    }

    private static CardmarketCard card(String id) {
        CardmarketCard card = new CardmarketCard();
        card.setId(id);
        card.setLink("https://example.test/" + id);
        card.setFoilType(CardFoilType.NO);
        return card;
    }

    private static CardmarketDeck deck(String id, String versionId) {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setId(id);
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setId(versionId);
        version.setDeck(deck);
        deck.setCurrentVersion(version);
        return deck;
    }

    private static CardmarketDeckCard deckCard(CardmarketDeckVersion version, CardmarketCard card) {
        CardmarketDeckCard deckCard = new CardmarketDeckCard();
        deckCard.setDeckVersion(version);
        deckCard.setCard(card);
        return deckCard;
    }
}
