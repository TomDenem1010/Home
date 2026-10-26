package trd.home.tcg.service.application;

import java.math.BigInteger;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.DeckEditor;
import trd.home.tcg.exception.DeckEditException;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@RequiredArgsConstructor
public class DeckEditService {
    private final CardmarketDeckRepository decks;
    private final CardmarketCardRepository cards;

    @Transactional(readOnly = true)
    public DeckEditor getDeck(String id) {
        var deck = activeDeck(id);
        var version = deck.getCurrentVersion();
        return new DeckEditor(
                id,
                deck.getName(),
                version.getId(),
                version.getVersion(),
                version.getCards().stream()
                        .map(e -> new DeckEditor.Card(
                                e.getCard().getId(),
                                e.getCard().getLink(),
                                e.getCard().getFoilType(),
                                e.getQuantity()))
                        .sorted(Comparator.comparing((DeckEditor.Card card) -> card.link())
                                .thenComparing(e -> e.foilType().name()))
                        .toList());
    }

    @Transactional
    public void removeCards(String id, String versionId, List<String> selected) {
        var deck = editableDeck(id, versionId);
        var source = deck.getCurrentVersion();
        if (selected == null || selected.isEmpty()) throw new DeckEditException("Select at least one card.");
        if (!source.getCards().stream().map(e -> e.getCard().getId()).toList().containsAll(selected))
            throw new DeckEditException("Selected cards do not belong to this version.");
        var next = nextVersion(deck);
        source.getCards().stream()
                .filter(e -> !selected.contains(e.getCard().getId()))
                .forEach(e -> next.addCard(e.getCard(), e.getQuantity()));
        deck.addVersion(next);
        decks.save(deck);
    }

    @Transactional
    public void addCard(String id, String versionId, int quantity, String link, CardFoilType foilType) {
        var deck = editableDeck(id, versionId);
        validateCardQuantityAndFoil(quantity, foilType);
        String normalizedLink = validatedCardLink(link);
        var source = deck.getCurrentVersion();
        var existing = findExistingCard(source, normalizedLink, foilType);
        int combinedQuantity = combinedQuantity(existing, quantity);
        var card = findOrCreateCard(normalizedLink, foilType);
        var next = copyVersionExcludingCard(deck, source, existing);
        next.addCard(card, combinedQuantity);
        deck.addVersion(next);
        decks.save(deck);
    }

    private void validateCardQuantityAndFoil(int quantity, CardFoilType foilType) {
        if (quantity <= 0 || foilType == null)
            throw new DeckEditException("Positive quantity and foil type are required.");
    }

    private String validatedCardLink(String link) {
        String normalized = link == null ? "" : link.strip();
        try {
            var uri = URI.create(normalized);
            if (normalized.length() > 255
                    || !"https".equals(uri.getScheme())
                    || !"www.cardmarket.com".equals(uri.getHost())
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || uri.getPath() == null
                    || !uri.getPath().contains("/Products/")) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new DeckEditException("Enter a valid HTTPS Cardmarket product URL.");
        }
        return normalized;
    }

    private Optional<CardmarketDeckCard> findExistingCard(
            CardmarketDeckVersion source, String link, CardFoilType foilType) {
        return source.getCards().stream()
                .filter(entry -> entry.getCard().getLink().equals(link)
                        && entry.getCard().getFoilType() == foilType)
                .findFirst();
    }

    private int combinedQuantity(Optional<CardmarketDeckCard> existing, int quantity) {
        int previous = existing.map(entry -> entry.getQuantity()).orElse(0);
        if (previous > Integer.MAX_VALUE - quantity) throw new DeckEditException("Card quantity is too large.");
        return previous + quantity;
    }

    private CardmarketCard findOrCreateCard(String link, CardFoilType foilType) {
        return cards.findByLinkAndFoilType(link, foilType).orElseGet(() -> {
            var newCard = new CardmarketCard();
            newCard.setLink(link);
            newCard.setFoilType(foilType);
            return cards.save(newCard);
        });
    }

    private CardmarketDeckVersion copyVersionExcludingCard(
            CardmarketDeck deck, CardmarketDeckVersion source, Optional<CardmarketDeckCard> excluded) {
        var next = nextVersion(deck);
        source.getCards().stream()
                .filter(entry -> excluded.isEmpty() || entry != excluded.get())
                .forEach(entry -> next.addCard(entry.getCard(), entry.getQuantity()));
        return next;
    }

    private CardmarketDeck activeDeck(String id) {
        var deck = decks.findById(id).orElseThrow(() -> new DeckEditException("Deck not found."));
        if (deck.getStatus() != DeckStatus.ACTIVE || deck.getCurrentVersion() == null)
            throw new DeckEditException("Only active decks with a current version can be edited.");
        return deck;
    }

    private CardmarketDeck editableDeck(String id, String versionId) {
        var deck = activeDeck(id);
        if (!Objects.equals(deck.getCurrentVersion().getId(), versionId))
            throw new DeckEditException("The deck has changed. Reload the page before editing.");
        return deck;
    }

    private CardmarketDeckVersion nextVersion(CardmarketDeck deck) {
        var number = deck.getVersions().stream()
                .map(v -> new BigInteger(v.getVersion().replaceFirst("^[vV]", "")))
                .max(Comparator.naturalOrder())
                .orElse(BigInteger.ZERO)
                .add(BigInteger.ONE);
        var next = new CardmarketDeckVersion();
        next.setVersion("v" + number);
        return next;
    }
}
