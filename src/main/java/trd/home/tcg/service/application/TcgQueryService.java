package trd.home.tcg.service.application;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dto.CardmarketDeckCardPriceSummary;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@RequiredArgsConstructor
public class TcgQueryService {

    private final CardmarketDeckRepository deckRepository;
    private final CardmarketDeckCardRepository deckCardRepository;
    private final CardmarketCardRepository cardRepository;
    private final CardmarketCardPriceRepository priceRepository;

    @Transactional(readOnly = true)
    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        List<CardmarketDeck> decks = deckRepository.findAllByStatusOrderByName(DeckStatus.ACTIVE);
        List<String> versionIds = decks.stream()
                .map(CardmarketDeck::getCurrentVersion)
                .filter(Objects::nonNull)
                .map(version -> version.getId())
                .toList();
        Map<String, List<CardmarketDeckCard>> cardsByVersion = versionIds.isEmpty()
                ? Map.of()
                : deckCardRepository.findAllByDeckVersionIdIn(versionIds).stream()
                        .collect(Collectors.groupingBy(
                                deckCard -> deckCard.getDeckVersion().getId()));
        Map<String, CardmarketCardPrice> latestPrices = latestPrices(
                cardsByVersion.values().stream().flatMap(Collection::stream).toList());

        return decks.stream()
                .map(deck -> summarizeDeck(deck, cardsByVersion, latestPrices))
                .toList();
    }

    @Transactional(readOnly = true)
    public CardmarketDeckPriceHistorySummary getDeckPriceHistorySummary(String deckId) {
        CardmarketDeck deck = deckRepository.findById(deckId).orElse(null);
        if (deck == null || deck.getCurrentVersion() == null) {
            return emptyHistory(deckId);
        }

        List<CardmarketDeckCard> deckCards = deckCardRepository.findAllByDeckVersionId(
                deck.getCurrentVersion().getId());
        Map<String, CardmarketCard> cards = cardsById(deckCards);
        Map<String, CardmarketCardPrice> latestPrices = latestPrices(deckCards);
        List<CardmarketDeckCardPriceSummary> summaries = deckCards.stream()
                .map(deckCard -> summarizeCard(deckCard, cards, latestPrices))
                .sorted(Comparator.comparing(CardmarketDeckCardPriceSummary::cardName))
                .toList();

        return new CardmarketDeckPriceHistorySummary(
                Objects.requireNonNullElse(deckId, ""),
                summaries,
                totalPrice(summaries, CardmarketDeckCardPriceSummary::latestFromInEuro),
                totalPrice(summaries, CardmarketDeckCardPriceSummary::latestTrendInEuro));
    }

    private CardmarketDeckPriceSummary summarizeDeck(
            CardmarketDeck deck,
            Map<String, List<CardmarketDeckCard>> cardsByVersion,
            Map<String, CardmarketCardPrice> latestPrices) {
        List<CardmarketDeckCard> deckCards = deck.getCurrentVersion() == null
                ? List.of()
                : cardsByVersion.getOrDefault(deck.getCurrentVersion().getId(), List.of());
        return new CardmarketDeckPriceSummary(
                Objects.requireNonNullElse(deck.getId(), ""),
                Objects.requireNonNullElse(deck.getName(), ""),
                totalPrice(deckCards, latestPrices, CardmarketCardPrice::getFromInEuro),
                totalPrice(deckCards, latestPrices, CardmarketCardPrice::getTrendInEuro));
    }

    private CardmarketDeckCardPriceSummary summarizeCard(
            CardmarketDeckCard deckCard,
            Map<String, CardmarketCard> cards,
            Map<String, CardmarketCardPrice> latestPrices) {
        String cardId = deckCard.getCard().getId();
        CardmarketCard card = cards.get(cardId);
        CardmarketCardPrice price = latestPrices.get(cardId);
        return new CardmarketDeckCardPriceSummary(
                cardName(card == null ? null : card.getLink()),
                card == null ? "" : Objects.requireNonNullElse(card.getLink(), ""),
                deckCard.getQuantity(),
                nullablePriceValue(price, CardmarketCardPrice::getFromInEuro),
                nullablePriceValue(price, CardmarketCardPrice::getTrendInEuro),
                price == null ? null : price.getCreatedAt());
    }

    private Map<String, CardmarketCard> cardsById(List<CardmarketDeckCard> deckCards) {
        return cardRepository.findAllById(distinctCardIds(deckCards)).stream()
                .collect(Collectors.toMap(CardmarketCard::getId, Function.identity()));
    }

    private Map<String, CardmarketCardPrice> latestPrices(List<CardmarketDeckCard> deckCards) {
        return distinctCardIds(deckCards).stream()
                .map(cardId -> priceRepository
                        .findFirstByCardIdAndFromInEuroNotOrCardIdAndTrendInEuroNotOrderByCreatedAtDescIdDesc(
                                cardId, BigDecimal.ZERO, cardId, BigDecimal.ZERO)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(price -> price.getCard().getId(), Function.identity()));
    }

    private static List<String> distinctCardIds(List<CardmarketDeckCard> deckCards) {
        return deckCards.stream()
                .map(deckCard -> deckCard.getCard().getId())
                .distinct()
                .toList();
    }

    private static BigDecimal totalPrice(
            List<CardmarketDeckCard> cards,
            Map<String, CardmarketCardPrice> latestPrices,
            Function<CardmarketCardPrice, BigDecimal> priceExtractor) {
        return cards.stream()
                .map(card -> priceValue(latestPrices.get(card.getCard().getId()), priceExtractor)
                        .multiply(BigDecimal.valueOf(card.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal priceValue(
            CardmarketCardPrice price, Function<CardmarketCardPrice, BigDecimal> priceExtractor) {
        return price == null
                ? BigDecimal.ZERO
                : Objects.requireNonNullElse(priceExtractor.apply(price), BigDecimal.ZERO);
    }

    private static BigDecimal nullablePriceValue(
            CardmarketCardPrice price, Function<CardmarketCardPrice, BigDecimal> priceExtractor) {
        return price == null ? null : priceExtractor.apply(price);
    }

    private static String cardName(String link) {
        if (link == null || link.isBlank()) {
            return "";
        }
        String path = URI.create(link).getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash < 0 ? path : path.substring(lastSlash + 1);
    }

    private static CardmarketDeckPriceHistorySummary emptyHistory(String deckId) {
        return new CardmarketDeckPriceHistorySummary(
                Objects.requireNonNullElse(deckId, ""), List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static BigDecimal totalPrice(
            List<CardmarketDeckCardPriceSummary> cards,
            Function<CardmarketDeckCardPriceSummary, BigDecimal> priceExtractor) {
        return cards.stream()
                .map(card -> Objects.requireNonNullElse(priceExtractor.apply(card), BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(card.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
