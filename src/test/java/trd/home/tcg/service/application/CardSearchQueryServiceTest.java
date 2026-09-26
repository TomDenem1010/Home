package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardPriceType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.dto.CardSearchPrice;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

class CardSearchQueryServiceTest {
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketDeckCardRepository deckCards = mock(CardmarketDeckCardRepository.class);
    private final CardmarketCardRepository cards = mock(CardmarketCardRepository.class);
    private final CardmarketCardPriceRepository prices = mock(CardmarketCardPriceRepository.class);
    private final TcgQueryService service = new TcgQueryService(decks, deckCards, cards, prices);

    @Test
    void combinesAllFiltersAndAggregatesCurrentActiveDecksWithoutDuplicatingCards() {
        var a = deck("a", "Alpha");
        var b = deck("b", "Beta");
        var match = card("match", "Magic", "Fire-Card?language=1&isFoil=Y", CardFoilType.FOIL);
        var nonFoil = card("nonfoil", "Magic", "Fire-Card", CardFoilType.NO);
        var wrongGame = card("wrong-game", "FleshAndBlood", "Fire-Card", CardFoilType.FOIL);
        var wrongName = card("wrong-name", "Magic", "Water-Card", CardFoilType.FOIL);
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of(a, b));
        when(deckCards.findAllByDeckVersionIdIn(anyCollection()))
                .thenReturn(List.of(
                        holding(a, match, 2),
                        holding(b, match, 3),
                        holding(a, nonFoil, 7),
                        holding(a, wrongGame, 1),
                        holding(a, wrongName, 1)));
        var closed = new AtomicBoolean();
        when(prices.findAllByCardIdInOrderByCreatedAtDescIdDesc(List.of("match")))
                .thenReturn(Stream.of(
                                new CardSearchPrice("match", BigDecimal.ZERO, null),
                                new CardSearchPrice("match", new BigDecimal("2"), new BigDecimal("5")),
                                new CardSearchPrice("match", new BigDecimal("1"), new BigDecimal("3")))
                        .onClose(() -> closed.set(true)));
        var filter = new CardSearchFilter();
        filter.setName(" FIRE ");
        filter.setCardGameType(CardGameType.MAGIC_THE_GATHERING);
        filter.setFoilType(CardFoilType.FOIL);
        filter.setPriceType(CardPriceType.TREND);
        filter.setPriceMin(new BigDecimal("5"));
        filter.setPriceMax(new BigDecimal("5"));

        var page = service.searchCards(filter, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        var result = page.getContent().getFirst();
        assertEquals("Fire-Card", result.name());
        assertEquals(match.getLink(), result.link());
        assertEquals(5, result.quantity());
        assertEquals(new BigDecimal("2"), result.priceFrom());
        assertEquals(new BigDecimal("5"), result.priceTrend());
        assertEquals(
                List.of("Alpha", "Beta"),
                result.decks().stream().map(deck -> deck.name()).toList());
        assertEquals(
                List.of(2, 3),
                result.decks().stream().map(deck -> deck.quantity()).toList());
        assertTrue(closed.get());
        verify(deckCards)
                .findAllByDeckVersionIdIn(
                        argThat(ids -> ids.size() == 2 && ids.containsAll(List.of("version-a", "version-b"))));
        verifyNoInteractions(cards);
    }

    @Test
    void appliesPriceFilterToLatestPriceRatherThanMatchingOldPrice() {
        var deck = deck("a", "Alpha");
        var card = card("c", "Magic", "Card", CardFoilType.NO);
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of(deck));
        when(deckCards.findAllByDeckVersionIdIn(anyCollection())).thenReturn(List.of(holding(deck, card, 1)));
        when(prices.findAllByCardIdInOrderByCreatedAtDescIdDesc(anyCollection()))
                .thenReturn(Stream.of(
                        new CardSearchPrice("c", new BigDecimal("20"), new BigDecimal("30")),
                        new CardSearchPrice("c", new BigDecimal("2"), new BigDecimal("3"))));
        var filter = new CardSearchFilter();
        filter.setPriceMax(new BigDecimal("5"));
        assertTrue(service.searchCards(filter, PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void sortsEntireResultBeforePagingAndKeepsUnknownPricesLastWhenDescending() {
        var deck = deck("a", "Alpha");
        var a = card("a", "Magic", "Alpha", CardFoilType.NO);
        var b = card("b", "Magic", "Beta", CardFoilType.NO);
        var c = card("c", "Magic", "Unknown", CardFoilType.NO);
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of(deck));
        when(deckCards.findAllByDeckVersionIdIn(anyCollection()))
                .thenReturn(List.of(holding(deck, a, 1), holding(deck, b, 2), holding(deck, c, 3)));
        when(prices.findAllByCardIdInOrderByCreatedAtDescIdDesc(anyCollection()))
                .thenAnswer(ignored -> Stream.of(
                        new CardSearchPrice("a", new BigDecimal("1"), new BigDecimal("2")),
                        new CardSearchPrice("b", new BigDecimal("10"), new BigDecimal("20"))));
        var sort = Sort.by(Sort.Direction.DESC, "priceFrom");
        var first = service.searchCards(new CardSearchFilter(), PageRequest.of(0, 1, sort));
        var last = service.searchCards(new CardSearchFilter(), PageRequest.of(2, 1, sort));
        assertEquals(3, first.getTotalElements());
        assertEquals(3, first.getTotalPages());
        assertEquals("Beta", first.getContent().getFirst().name());
        assertEquals("Unknown", last.getContent().getFirst().name());
        assertNull(last.getContent().getFirst().priceFrom());
    }

    @Test
    void emptyActiveDecksDoNotTriggerCardOrPriceQueries() {
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of());
        assertTrue(service.searchCards(new CardSearchFilter(), PageRequest.of(0, 10))
                .isEmpty());
        verifyNoInteractions(deckCards, cards, prices);
    }

    private static CardmarketDeck deck(String id, String name) {
        var deck = new CardmarketDeck();
        deck.setId(id);
        deck.setName(name);
        var version = new CardmarketDeckVersion();
        version.setId("version-" + id);
        version.setVersion("v2");
        deck.addVersion(version);
        return deck;
    }

    private static CardmarketCard card(String id, String game, String name, CardFoilType foil) {
        var card = new CardmarketCard();
        card.setId(id);
        card.setLink("https://www.cardmarket.com/en/" + game + "/Products/Singles/Set/" + name);
        card.setFoilType(foil);
        return card;
    }

    private static CardmarketDeckCard holding(CardmarketDeck deck, CardmarketCard card, int quantity) {
        var holding = new CardmarketDeckCard();
        holding.setCard(card);
        holding.setDeckVersion(deck.getCurrentVersion());
        holding.setQuantity(quantity);
        return holding;
    }
}
