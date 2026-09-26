package trd.home.tcg.service.application;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.tcg.constant.CardPriceType;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dto.CardSearchDeck;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.dto.CardSearchPrice;
import trd.home.tcg.dto.CardSearchResult;
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

    private static final int SEARCH_BATCH_SIZE = 500;

    private final CardmarketDeckRepository deckRepository;
    private final CardmarketDeckCardRepository deckCardRepository;
    private final CardmarketCardRepository cardRepository;
    private final CardmarketCardPriceRepository priceRepository;

    @Transactional(readOnly = true)
    public Page<CardSearchResult> searchCards(CardSearchFilter filter, Pageable pageable) {
        Map<String, CardmarketDeck> decksByVersion = activeDecksByCurrentVersion();
        Map<String, List<CardmarketDeckCard>> cardsById =
                matchingCardsById(new ArrayList<>(decksByVersion.keySet()), filter);
        Map<String, CardSearchPrice> prices = latestSearchPrices(new ArrayList<>(cardsById.keySet()));
        List<CardSearchResult> matches = searchResults(cardsById, decksByVersion, prices, filter, pageable.getSort());
        return searchPage(matches, pageable);
    }

    @Transactional(readOnly = true)
    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        List<CardmarketDeck> decks = deckRepository.findAllByStatusOrderByName(DeckStatus.ACTIVE);
        Map<String, List<CardmarketDeckCard>> cardsByVersion = cardsByCurrentVersion(decks);
        Map<String, CardmarketCardPrice> latestPrices = latestPrices(flattenDeckCards(cardsByVersion));
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
        return summarizeHistory(deckId, cardPriceSummaries(deckCards));
    }

    private Map<String, CardmarketDeck> activeDecksByCurrentVersion() {
        return deckRepository.findAllByStatusOrderByName(DeckStatus.ACTIVE).stream()
                .filter(deck -> deck.getCurrentVersion() != null)
                .collect(Collectors.toMap(deck -> deck.getCurrentVersion().getId(), Function.identity()));
    }

    private Map<String, List<CardmarketDeckCard>> matchingCardsById(List<String> versionIds, CardSearchFilter filter) {
        Map<String, List<CardmarketDeckCard>> cardsById = new HashMap<>();
        for (int offset = 0; offset < versionIds.size(); offset += SEARCH_BATCH_SIZE) {
            groupMatchingCards(
                    deckCardRepository.findAllByDeckVersionIdIn(searchBatch(versionIds, offset)), filter, cardsById);
        }
        return cardsById;
    }

    private static void groupMatchingCards(
            List<CardmarketDeckCard> cards, CardSearchFilter filter, Map<String, List<CardmarketDeckCard>> cardsById) {
        cards.stream().filter(card -> matchesCard(card.getCard(), filter)).forEach(card -> cardsById
                .computeIfAbsent(card.getCard().getId(), ignored -> new ArrayList<>())
                .add(card));
    }

    private static List<CardSearchResult> searchResults(
            Map<String, List<CardmarketDeckCard>> cardsById,
            Map<String, CardmarketDeck> decksByVersion,
            Map<String, CardSearchPrice> prices,
            CardSearchFilter filter,
            Sort sort) {
        return cardsById.entrySet().stream()
                .map(entry -> searchResult(entry.getValue(), decksByVersion, prices.get(entry.getKey())))
                .filter(result -> matchesPrice(result, filter))
                .sorted(searchOrder(sort))
                .toList();
    }

    private static Page<CardSearchResult> searchPage(List<CardSearchResult> matches, Pageable pageable) {
        int start = (int) Math.min(pageable.getOffset(), matches.size());
        int end = (int) Math.min((long) start + pageable.getPageSize(), matches.size());
        return new PageImpl<>(matches.subList(start, end), pageable, matches.size());
    }

    private static boolean matchesCard(CardmarketCard card, CardSearchFilter filter) {
        return matchesFoilType(card, filter) && matchesName(card, filter) && matchesGameType(card, filter);
    }

    private static boolean matchesFoilType(CardmarketCard card, CardSearchFilter filter) {
        return filter.getFoilType() == null || card.getFoilType() == filter.getFoilType();
    }

    private static boolean matchesName(CardmarketCard card, CardSearchFilter filter) {
        return filter.getName() == null
                || filter.getName().isBlank()
                || cardName(card.getLink())
                        .toLowerCase(Locale.ROOT)
                        .contains(filter.getName().strip().toLowerCase(Locale.ROOT));
    }

    private static boolean matchesGameType(CardmarketCard card, CardSearchFilter filter) {
        return filter.getCardGameType() == null
                || URI.create(card.getLink())
                        .getPath()
                        .contains("/" + filter.getCardGameType().getCardmarketUrlPart() + "/");
    }

    private Map<String, CardSearchPrice> latestSearchPrices(List<String> cardIds) {
        Map<String, CardSearchPrice> latest = new HashMap<>();
        for (int offset = 0; offset < cardIds.size(); offset += SEARCH_BATCH_SIZE) {
            collectLatestSearchPrices(searchBatch(cardIds, offset), latest);
        }
        return latest;
    }

    private void collectLatestSearchPrices(List<String> cardIds, Map<String, CardSearchPrice> latest) {
        var remaining = new HashSet<>(cardIds);
        try (Stream<CardSearchPrice> prices = priceRepository.findAllByCardIdInOrderByCreatedAtDescIdDesc(cardIds)) {
            prices.takeWhile(ignored -> !remaining.isEmpty())
                    .filter(price -> isKnownPrice(price.fromInEuro()) || isKnownPrice(price.trendInEuro()))
                    .forEach(price -> {
                        latest.putIfAbsent(price.cardId(), price);
                        remaining.remove(price.cardId());
                    });
        }
    }

    private static List<String> searchBatch(List<String> ids, int offset) {
        return ids.subList(offset, Math.min(offset + SEARCH_BATCH_SIZE, ids.size()));
    }

    private static boolean isKnownPrice(BigDecimal price) {
        return price != null && price.signum() != 0;
    }

    private static CardSearchResult searchResult(
            List<CardmarketDeckCard> cards, Map<String, CardmarketDeck> decksByVersion, CardSearchPrice price) {
        CardmarketCard card = cards.getFirst().getCard();
        return new CardSearchResult(
                card.getId(),
                cardName(card.getLink()),
                card.getLink(),
                card.getFoilType(),
                cards.stream().mapToLong(CardmarketDeckCard::getQuantity).sum(),
                price == null ? null : price.fromInEuro(),
                price == null ? null : price.trendInEuro(),
                searchDecks(cards, decksByVersion));
    }

    private static List<CardSearchDeck> searchDecks(
            List<CardmarketDeckCard> cards, Map<String, CardmarketDeck> decksByVersion) {
        return cards.stream()
                .map(card -> searchDeck(
                        card, decksByVersion.get(card.getDeckVersion().getId())))
                .sorted(Comparator.comparing(CardSearchDeck::name).thenComparing(CardSearchDeck::id))
                .toList();
    }

    private static CardSearchDeck searchDeck(CardmarketDeckCard card, CardmarketDeck deck) {
        return new CardSearchDeck(deck.getId(), deck.getName(), card.getQuantity());
    }

    private static boolean matchesPrice(CardSearchResult card, CardSearchFilter filter) {
        if (filter.getPriceMin() == null && filter.getPriceMax() == null) return true;
        BigDecimal price = filter.getPriceType() == CardPriceType.TREND ? card.priceTrend() : card.priceFrom();
        return price != null
                && (filter.getPriceMin() == null || price.compareTo(filter.getPriceMin()) >= 0)
                && (filter.getPriceMax() == null || price.compareTo(filter.getPriceMax()) <= 0);
    }

    private static Comparator<CardSearchResult> searchOrder(Sort sort) {
        Sort.Order order = sort.stream().findFirst().orElse(Sort.Order.asc("name"));
        Comparator<CardSearchResult> comparator =
                switch (order.getProperty()) {
                    case "foilType" ->
                        compareSearchValue(card -> card.foilType().name(), String.CASE_INSENSITIVE_ORDER, order);
                    case "quantity" -> compareSearchValue(CardSearchResult::quantity, Comparator.naturalOrder(), order);
                    case "priceFrom" ->
                        compareSearchValue(CardSearchResult::priceFrom, Comparator.naturalOrder(), order);
                    case "priceTrend" ->
                        compareSearchValue(CardSearchResult::priceTrend, Comparator.naturalOrder(), order);
                    case "decks" ->
                        compareSearchValue(
                                card -> card.decks().getFirst().name(), String.CASE_INSENSITIVE_ORDER, order);
                    default -> compareSearchValue(CardSearchResult::name, String.CASE_INSENSITIVE_ORDER, order);
                };
        return comparator.thenComparing(CardSearchResult::id);
    }

    private static <T> Comparator<CardSearchResult> compareSearchValue(
            Function<CardSearchResult, T> value, Comparator<T> comparator, Sort.Order order) {
        return Comparator.comparing(
                value, Comparator.nullsLast(order.isDescending() ? comparator.reversed() : comparator));
    }

    private Map<String, List<CardmarketDeckCard>> cardsByCurrentVersion(List<CardmarketDeck> decks) {
        List<String> versionIds = currentVersionIds(decks);
        return versionIds.isEmpty()
                ? Map.of()
                : deckCardRepository.findAllByDeckVersionIdIn(versionIds).stream()
                        .collect(Collectors.groupingBy(
                                card -> card.getDeckVersion().getId()));
    }

    private static List<String> currentVersionIds(List<CardmarketDeck> decks) {
        return decks.stream()
                .map(deck -> deck.getCurrentVersion())
                .filter(Objects::nonNull)
                .map(version -> version.getId())
                .toList();
    }

    private static List<CardmarketDeckCard> flattenDeckCards(Map<String, List<CardmarketDeckCard>> cardsByVersion) {
        return cardsByVersion.values().stream().flatMap(List::stream).toList();
    }

    private List<CardmarketDeckCardPriceSummary> cardPriceSummaries(List<CardmarketDeckCard> deckCards) {
        Map<String, CardmarketCard> cards = cardsById(deckCards);
        Map<String, CardmarketCardPrice> latestPrices = latestPrices(deckCards);
        return deckCards.stream()
                .map(deckCard -> summarizeCard(deckCard, cards, latestPrices))
                .sorted(Comparator.comparing(CardmarketDeckCardPriceSummary::cardName))
                .toList();
    }

    private static CardmarketDeckPriceHistorySummary summarizeHistory(
            String deckId, List<CardmarketDeckCardPriceSummary> summaries) {
        return new CardmarketDeckPriceHistorySummary(
                Objects.requireNonNullElse(deckId, ""),
                summaries,
                totalPrice(summaries, summary -> summary.latestFromInEuro()),
                totalPrice(summaries, summary -> summary.latestTrendInEuro()));
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
                totalPrice(deckCards, latestPrices, price -> price.getFromInEuro()),
                totalPrice(deckCards, latestPrices, price -> price.getTrendInEuro()));
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
                nullablePriceValue(price, cardPrice -> cardPrice.getFromInEuro()),
                nullablePriceValue(price, cardPrice -> cardPrice.getTrendInEuro()),
                price == null ? null : price.getCreatedAt());
    }

    private Map<String, CardmarketCard> cardsById(List<CardmarketDeckCard> deckCards) {
        return cardRepository.findAllById(distinctCardIds(deckCards)).stream()
                .collect(Collectors.toMap(card -> card.getId(), Function.identity()));
    }

    private Map<String, CardmarketCardPrice> latestPrices(List<CardmarketDeckCard> deckCards) {
        return distinctCardIds(deckCards).stream()
                .map(this::latestPrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(price -> price.getCard().getId(), Function.identity()));
    }

    private CardmarketCardPrice latestPrice(String cardId) {
        return priceRepository
                .findFirstByCardIdAndFromInEuroNotOrCardIdAndTrendInEuroNotOrderByCreatedAtDescIdDesc(
                        cardId, BigDecimal.ZERO, cardId, BigDecimal.ZERO)
                .orElse(null);
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
                .reduce(BigDecimal.ZERO, (total, price) -> total.add(price));
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
                .reduce(BigDecimal.ZERO, (total, price) -> total.add(price));
    }
}
