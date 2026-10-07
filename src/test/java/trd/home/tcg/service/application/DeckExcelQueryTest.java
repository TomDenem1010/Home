package trd.home.tcg.service.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.CardSearchPrice;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

class DeckExcelQueryTest {

    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketDeckCardRepository deckCards = mock(CardmarketDeckCardRepository.class);
    private final CardmarketCardRepository cards = mock(CardmarketCardRepository.class);
    private final CardmarketCardPriceRepository prices = mock(CardmarketCardPriceRepository.class);
    private final TcgQueryService service = new TcgQueryService(decks, deckCards, cards, prices);

    @Test
    void exportsActiveCurrentVersionsWithQuantitiesFoilsLatestPricesAndMissingPrices() {
        var first = deck("First", "current-first", "v2");
        var second = deck("Second", "current-second", "v3");
        var empty = new CardmarketDeck();
        empty.setName("Empty");
        var firstCard = card(first, "shared", "https://example.test/Z", 3, CardFoilType.FOIL);
        var unpriced = card(first, "missing", "https://example.test/A", 4, CardFoilType.NO);
        var secondCard = card(second, "shared", "https://example.test/Z", 1, CardFoilType.FOIL);
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of(empty, first, second));
        when(deckCards.findAllByDeckVersionIdIn(List.of("current-first", "current-second")))
                .thenReturn(List.of(firstCard, unpriced, secondCard));
        Runnable closed = mock(Runnable.class);
        when(prices.findAllByCardIdInOrderByCreatedAtDescIdDesc(anyCollection()))
                .thenReturn(Stream.of(
                                new CardSearchPrice("shared", new BigDecimal("1.2500"), null),
                                new CardSearchPrice("shared", new BigDecimal("9"), new BigDecimal("10")))
                        .onClose(closed));

        var result = service.getDeckExcelData();

        assertEquals(3, result.size());
        assertEquals("Empty", result.get(0).name());
        assertEquals("", result.get(0).version());
        assertTrue(result.get(0).cards().isEmpty());
        assertEquals("v2", result.get(1).version());
        var exported = result.get(1).cards();
        assertEquals("https://example.test/A", exported.get(0).link());
        assertEquals(4, exported.get(0).quantity());
        assertNull(exported.get(0).fromEuro());
        assertEquals(3, exported.get(1).quantity());
        assertEquals(CardFoilType.FOIL, exported.get(1).foilType());
        assertEquals(new BigDecimal("1.2500"), exported.get(1).fromEuro());
        assertNull(exported.get(1).trendEuro());
        assertEquals(1, result.get(2).cards().getFirst().quantity());
        verify(decks).findAllByStatusOrderByName(DeckStatus.ACTIVE);
        verify(deckCards).findAllByDeckVersionIdIn(List.of("current-first", "current-second"));
        verify(prices, times(1)).findAllByCardIdInOrderByCreatedAtDescIdDesc(anyCollection());
        verify(closed).run();
        verifyNoInteractions(cards);
    }

    @Test
    void emptyLibraryAvoidsCardAndPriceQueries() {
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of());
        assertTrue(service.getDeckExcelData().isEmpty());
        verifyNoInteractions(deckCards, cards, prices);
    }

    @Test
    void batchesPriceQueriesAndClosesEveryStream() {
        var deck = deck("Deck", "version", "v1");
        var entries = IntStream.range(0, 501)
                .mapToObj(index -> card(deck, "card-" + index, "https://example.test/" + index, 1, CardFoilType.NO))
                .toList();
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenReturn(List.of(deck));
        when(deckCards.findAllByDeckVersionIdIn(List.of("version"))).thenReturn(entries);
        Runnable closed = mock(Runnable.class);
        when(prices.findAllByCardIdInOrderByCreatedAtDescIdDesc(anyCollection()))
                .thenAnswer(invocation -> {
                    List<String> ids = invocation.getArgument(0);
                    assertTrue(ids.size() <= 500);
                    return ids.stream()
                            .map(id -> new CardSearchPrice(id, BigDecimal.ONE, BigDecimal.TEN))
                            .onClose(closed);
                });
        var result = service.getDeckExcelData();
        assertEquals(501, result.getFirst().cards().size());
        assertTrue(result.getFirst().cards().stream().allMatch(card -> BigDecimal.ONE.equals(card.fromEuro())));
        verify(prices, times(2)).findAllByCardIdInOrderByCreatedAtDescIdDesc(anyCollection());
        verify(closed, times(2)).run();
    }

    @Test
    void propagatesRepositoryFailures() {
        var failure = new IllegalStateException("Database unavailable");
        when(decks.findAllByStatusOrderByName(DeckStatus.ACTIVE)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, service::getDeckExcelData));
        verifyNoInteractions(deckCards, cards, prices);
    }

    private static CardmarketDeck deck(String name, String versionId, String versionName) {
        var deck = new CardmarketDeck();
        deck.setName(name);
        var version = new CardmarketDeckVersion();
        version.setId(versionId);
        version.setVersion(versionName);
        deck.addVersion(version);
        return deck;
    }

    private static CardmarketDeckCard card(
            CardmarketDeck deck, String id, String link, int quantity, CardFoilType foil) {
        var card = new CardmarketCard();
        card.setId(id);
        card.setLink(link);
        card.setFoilType(foil);
        var entry = new CardmarketDeckCard();
        entry.setDeckVersion(deck.getCurrentVersion());
        entry.setCard(card);
        entry.setQuantity(quantity);
        return entry;
    }
}
