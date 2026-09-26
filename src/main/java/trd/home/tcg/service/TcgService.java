package trd.home.tcg.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardPriceType;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.dto.CardSearchResult;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.service.application.TcgCommandService;
import trd.home.tcg.service.application.TcgQueryService;

@Service
@RequiredArgsConstructor
public class TcgService {

    private final TcgCommandService commands;
    private final TcgQueryService queries;

    public CardFoilType[] getCardFoilTypes() {
        return CardFoilType.values();
    }

    public CardGameType[] getCardGameTypes() {
        return CardGameType.values();
    }

    public CardPriceType[] getCardPriceTypes() {
        return CardPriceType.values();
    }

    @LogMethodCall
    public Page<CardSearchResult> searchCards(
            CardSearchFilter filter, int page, int size, String sort, String direction) {
        return queries.searchCards(
                filter, PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort)));
    }

    @LogMethodCall
    public void saveDecksFromResource() {
        commands.saveDecksFromResource(null);
    }

    @LogMethodCall
    public void saveDeckFromResource(String deckId) {
        commands.saveDecksFromResource(deckId);
    }

    @LogMethodCall
    public void refreshDeckPrices(String deckId) {
        commands.refreshDeckPrices(deckId);
    }

    @LogMethodCall
    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        return queries.getDeckPriceSummary();
    }

    @LogMethodCall
    public CardmarketDeckPriceHistorySummary getDeckPriceHistorySummary(String deckId) {
        return queries.getDeckPriceHistorySummary(deckId);
    }
}
