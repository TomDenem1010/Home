package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

class TcgApplicationServiceTest {

    private final ApplicationEventQueue events = mock(ApplicationEventQueue.class);
    private final FrontendNotificationPublisher notifications = mock(FrontendNotificationPublisher.class);
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketDeckCardRepository deckCards = mock(CardmarketDeckCardRepository.class);
    private final CardmarketCardRepository cards = mock(CardmarketCardRepository.class);
    private final CardmarketCardPriceRepository prices = mock(CardmarketCardPriceRepository.class);

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
    void calculatesActiveDeckPriceSummariesFromEntities() {
        CardmarketCard card = card("card-id", "https://example.test/Card");
        CardmarketDeck deck = deck("deck-id", "Deck", "version-id");
        CardmarketDeckCard deckCard = deckCard(deck.getCurrentVersion(), card, 2);
        CardmarketCardPrice price = price(card, "5.25", "5.75", Instant.parse("2026-01-01T00:00:00Z"));
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of(deck));
        when(deckCards.findAllByDeckVersionIdIn(List.of("version-id"))).thenReturn(List.of(deckCard));
        when(prices.findFirstByCardIdAndFromInEuroNotOrCardIdAndTrendInEuroNotOrderByCreatedAtDescIdDesc(
                        "card-id", BigDecimal.ZERO, "card-id", BigDecimal.ZERO))
                .thenReturn(Optional.of(price));

        List<CardmarketDeckPriceSummary> result = service().getDeckPriceSummary();

        assertEquals(1, result.size());
        assertEquals("deck-id", result.getFirst().deckId());
        assertEquals(new BigDecimal("10.50"), result.getFirst().sumFromInEuro());
        assertEquals(new BigDecimal("11.50"), result.getFirst().sumTrendInEuro());
    }

    @Test
    void calculatesHistoryAndTotalsWithUnpricedCards() {
        CardmarketCard pricedCard = card("priced", "https://example.test/Card");
        CardmarketCard unpricedCard = card("unpriced", "https://example.test/Unpriced");
        CardmarketDeck deck = deck("deck-id", "Deck", "version-id");
        List<CardmarketDeckCard> versionCards = List.of(
                deckCard(deck.getCurrentVersion(), pricedCard, 2),
                deckCard(deck.getCurrentVersion(), unpricedCard, 100));
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        CardmarketCardPrice price = price(pricedCard, "3.00", "4.00", createdAt);
        when(decks.findById("deck-id")).thenReturn(Optional.of(deck));
        when(deckCards.findAllByDeckVersionId("version-id")).thenReturn(versionCards);
        when(cards.findAllById(List.of("priced", "unpriced"))).thenReturn(List.of(pricedCard, unpricedCard));
        when(prices.findFirstByCardIdAndFromInEuroNotOrCardIdAndTrendInEuroNotOrderByCreatedAtDescIdDesc(
                        "priced", BigDecimal.ZERO, "priced", BigDecimal.ZERO))
                .thenReturn(Optional.of(price));

        CardmarketDeckPriceHistorySummary result = service().getDeckPriceHistorySummary("deck-id");

        assertEquals("deck-id", result.deckId());
        assertEquals(2, result.cards().size());
        assertEquals(createdAt, result.cards().getFirst().latestPriceCreatedAt());
        assertNull(result.cards().getLast().latestPriceCreatedAt());
        assertNull(result.cards().getLast().latestFromInEuro());
        assertNull(result.cards().getLast().latestTrendInEuro());
        assertEquals(new BigDecimal("6.00"), result.sumLatestFromInEuro());
        assertEquals(new BigDecimal("8.00"), result.sumLatestTrendInEuro());
    }

    private TcgQueryService service() {
        return new TcgQueryService(decks, deckCards, cards, prices);
    }

    private static CardmarketDeck deck(String id, String name, String versionId) {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setId(id);
        deck.setName(name);
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setId(versionId);
        version.setDeck(deck);
        deck.setCurrentVersion(version);
        return deck;
    }

    private static CardmarketCard card(String id, String link) {
        CardmarketCard card = new CardmarketCard();
        card.setId(id);
        card.setLink(link);
        return card;
    }

    private static CardmarketDeckCard deckCard(CardmarketDeckVersion version, CardmarketCard card, int quantity) {
        CardmarketDeckCard deckCard = new CardmarketDeckCard();
        deckCard.setDeckVersion(version);
        deckCard.setCard(card);
        deckCard.setQuantity(quantity);
        return deckCard;
    }

    private static CardmarketCardPrice price(CardmarketCard card, String from, String trend, Instant createdAt) {
        CardmarketCardPrice price = mock(CardmarketCardPrice.class);
        when(price.getCard()).thenReturn(card);
        when(price.getFromInEuro()).thenReturn(new BigDecimal(from));
        when(price.getTrendInEuro()).thenReturn(new BigDecimal(trend));
        when(price.getCreatedAt()).thenReturn(createdAt);
        return price;
    }
}
