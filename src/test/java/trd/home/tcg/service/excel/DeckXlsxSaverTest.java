package trd.home.tcg.service.excel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.*;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.repository.*;

class DeckXlsxSaverTest {
    private final CardmarketDeckRepository decks = mock(CardmarketDeckRepository.class);
    private final CardmarketCardRepository cards = mock(CardmarketCardRepository.class);
    private final CardmarketCardPriceRepository prices = mock(CardmarketCardPriceRepository.class);
    private final DeckXlsxSaver saver = new DeckXlsxSaver(decks, cards, prices);

    @Test
    void createsDeckWithSpecifiedVersionQuantitiesAndPrices() {
        when(cards.save(any())).thenAnswer(invocation -> {
            CardmarketCard card = invocation.getArgument(0);
            card.setId("card-id");
            return card;
        });
        assertTrue(saver.save(data("v42")));
        var saved = ArgumentCaptor.forClass(CardmarketDeck.class);
        verify(decks).save(saved.capture());
        var deck = saved.getValue();
        assertEquals("Deck", deck.getName());
        assertEquals("v42", deck.getCurrentVersion().getVersion());
        assertSame(deck, deck.getCurrentVersion().getDeck());
        var deckCard = deck.getCurrentVersion().getCards().iterator().next();
        assertEquals(3, deckCard.getQuantity());
        assertEquals("link", deckCard.getCard().getLink());
        assertEquals(CardFoilType.NO, deckCard.getCard().getFoilType());
        var price = ArgumentCaptor.forClass(CardmarketCardPrice.class);
        verify(prices).save(price.capture());
        assertSame(deckCard.getCard(), price.getValue().getCard());
        assertEquals(new BigDecimal("1.25"), price.getValue().getFromInEuro());
        assertEquals(new BigDecimal("2.5"), price.getValue().getTrendInEuro());
    }

    @Test
    void addsNewerVersionPreservingHistoryAndReusesCardsAndPriceRows() {
        var deck = existing("v9");
        var oldVersion = deck.getCurrentVersion();
        var card = new CardmarketCard();
        card.setId("card-id");
        card.setLink("link");
        var price = new CardmarketCardPrice();
        price.setCard(card);
        when(cards.findByLinkAndFoilType("link", CardFoilType.NO)).thenReturn(Optional.of(card));
        when(prices.findByCardId("card-id")).thenReturn(Optional.of(price));
        assertTrue(saver.save(data("v10")));
        assertEquals(List.of(oldVersion, deck.getCurrentVersion()), deck.getVersions());
        assertEquals("v10", deck.getCurrentVersion().getVersion());
        verify(cards, never()).save(any());
        verify(prices).save(price);
        verify(decks).save(deck);
    }

    @Test
    void skipsEqualAndOlderVersionsWithoutTouchingCardsPricesOrDeck() {
        existing("v10");
        for (var version : List.of("v10", "V10", "10", "v9")) {
            assertFalse(saver.save(data(version)));
        }
        verifyNoInteractions(cards, prices);
        verify(decks, never()).save(any());
    }

    @Test
    void createsEmptyVersionAndDoesNotCreatePriceForUnpricedCard() {
        when(cards.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var unpriced = new DeckExcelData.Card(1, "link", CardFoilType.NO, null, null);
        assertTrue(saver.save(new DeckExcelData("Deck", "v1", List.of(unpriced))));
        verifyNoInteractions(prices);
        assertTrue(saver.save(new DeckExcelData("Empty", "v5", List.of())));
    }

    @Test
    void propagatesPersistenceFailures() {
        when(cards.save(any())).thenThrow(new IllegalStateException("database failure"));
        assertThrows(IllegalStateException.class, () -> saver.save(data("v1")));
        verify(decks, never()).save(any());
    }

    @Test
    void persistsFoilTypeWhenCreatingPreviouslyUnknownCard() {
        when(cards.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertTrue(saver.save(new DeckExcelData(
                "Foils", "v1", List.of(new DeckExcelData.Card(1, "foil-link", CardFoilType.FOIL, null, null)))));
        var saved = ArgumentCaptor.forClass(CardmarketCard.class);
        verify(cards).save(saved.capture());
        assertEquals(CardFoilType.FOIL, saved.getValue().getFoilType());
    }

    private CardmarketDeck existing(String version) {
        var deck = new CardmarketDeck();
        deck.setName("Deck");
        var current = new CardmarketDeckVersion();
        current.setVersion(version);
        deck.addVersion(current);
        when(decks.findByName("Deck")).thenReturn(Optional.of(deck));
        return deck;
    }

    private static DeckExcelData data(String version) {
        return new DeckExcelData(
                "Deck",
                version,
                List.of(new DeckExcelData.Card(
                        3, "link", CardFoilType.NO, new BigDecimal("1.25"), new BigDecimal("2.5"))));
    }
}
