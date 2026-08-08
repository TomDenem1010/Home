package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

@ExtendWith(MockitoExtension.class)
class CardmarketDeckSaverTest {

    @Mock
    private CardmarketDeckRepository deckRepository;

    @Mock
    private CardmarketCardRepository cardRepository;

    @InjectMocks
    private CardmarketDeckSaver saver;

    @Test
    void doesNotSaveDeckThatAlreadyExists() {
        CardmarketDeck deck = deckWithCard(new CardmarketCard());
        deck.setName("Kilo Apogee Mind");
        when(deckRepository.existsByName(deck.getName())).thenReturn(true);

        saver.save(deck);

        verify(deckRepository).existsByName("Kilo Apogee Mind");
        verify(deckRepository, never()).save(any());
        verifyNoInteractions(cardRepository);
    }

    @Test
    void reusesExistingCardAndSavesNewDeck() {
        CardmarketCard importedCard = card("https://www.cardmarket.com/card", CardFoilType.FOIL);
        CardmarketDeck deck = deckWithCard(importedCard);
        CardmarketCard existingCard = card("https://www.cardmarket.com/card", CardFoilType.FOIL);
        when(deckRepository.existsByName(deck.getName())).thenReturn(false);
        when(cardRepository.findByLinkAndFoilType(importedCard.getLink(), importedCard.getFoilType()))
                .thenReturn(Optional.of(existingCard));

        saver.save(deck);

        CardmarketDeckCard deckCard =
                deck.getCurrentVersion().getCards().iterator().next();
        assertSame(existingCard, deckCard.getCard());
        verify(cardRepository).findByLinkAndFoilType(importedCard.getLink(), importedCard.getFoilType());
        verify(cardRepository, never()).save(any());
        verify(deckRepository).save(deck);
    }

    @Test
    void addsNewerVersionToExistingDeck() {
        CardmarketDeck existingDeck = deckWithCard(card("https://www.cardmarket.com/old", CardFoilType.FOIL));
        CardmarketDeck importedDeck = deckWithCard(card("https://www.cardmarket.com/new", CardFoilType.FOIL));
        importedDeck.getCurrentVersion().setVersion("v10");
        when(deckRepository.existsByName(importedDeck.getName())).thenReturn(true);
        when(deckRepository.findByName(importedDeck.getName())).thenReturn(Optional.of(existingDeck));
        when(cardRepository.findByLinkAndFoilType("https://www.cardmarket.com/new", CardFoilType.FOIL))
                .thenReturn(Optional.of(importedDeck
                        .getCurrentVersion()
                        .getCards()
                        .iterator()
                        .next()
                        .getCard()));

        saver.save(importedDeck);

        assertEquals(2, existingDeck.getVersions().size());
        assertSame(importedDeck.getCurrentVersion(), existingDeck.getCurrentVersion());
        verify(deckRepository).save(existingDeck);
    }

    @Test
    void doesNotSaveAnOlderOrEqualVersionForAnExistingDeck() {
        CardmarketDeck existingDeck = deckWithCard(card("https://www.cardmarket.com/current", CardFoilType.FOIL));
        existingDeck.getCurrentVersion().setVersion("v2");
        CardmarketDeck importedDeck = deckWithCard(card("https://www.cardmarket.com/imported", CardFoilType.FOIL));
        importedDeck.getCurrentVersion().setVersion("v2");
        when(deckRepository.existsByName(importedDeck.getName())).thenReturn(true);
        when(deckRepository.findByName(importedDeck.getName())).thenReturn(Optional.of(existingDeck));

        saver.save(importedDeck);

        assertEquals(1, existingDeck.getVersions().size());
        verify(deckRepository, never()).save(any());
        verifyNoInteractions(cardRepository);
    }

    @Test
    void savesCardThatDoesNotExistYetBeforeSavingDeck() {
        CardmarketCard importedCard = card("https://www.cardmarket.com/card", CardFoilType.ETCHED_FOIL);
        CardmarketCard persistedCard = card("https://www.cardmarket.com/card", CardFoilType.ETCHED_FOIL);
        CardmarketDeck deck = deckWithCard(importedCard);
        when(deckRepository.existsByName(deck.getName())).thenReturn(false);
        when(cardRepository.findByLinkAndFoilType(importedCard.getLink(), importedCard.getFoilType()))
                .thenReturn(Optional.empty());
        when(cardRepository.save(importedCard)).thenReturn(persistedCard);

        saver.save(deck);

        verify(cardRepository).save(importedCard);
        assertSame(
                persistedCard,
                deck.getCurrentVersion().getCards().iterator().next().getCard());
        verify(deckRepository).save(deck);
    }

    private static CardmarketDeck deckWithCard(CardmarketCard card) {
        CardmarketDeck deck = new CardmarketDeck();
        deck.setName("Kilo Apogee Mind");
        CardmarketDeckVersion version = new CardmarketDeckVersion();
        version.setVersion("v1");
        version.addCard(card, 1);
        deck.addVersion(version);
        return deck;
    }

    private static CardmarketCard card(String link, CardFoilType foilType) {
        CardmarketCard card = new CardmarketCard();
        card.setLink(link);
        card.setFoilType(foilType);
        return card;
    }
}
