package trd.home.tcg.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
import trd.home.tcg.dto.DeckEditor;
import trd.home.tcg.dto.DeckVersionHistory;
import trd.home.tcg.dto.DeckVersionListItem;
import trd.home.tcg.service.application.DeckCsvExportCommandService;
import trd.home.tcg.service.application.DeckCsvImportService;
import trd.home.tcg.service.application.DeckEditService;
import trd.home.tcg.service.application.DeckExcelExportCommandService;
import trd.home.tcg.service.application.DeckExcelImportService;
import trd.home.tcg.service.application.TcgCommandService;
import trd.home.tcg.service.application.TcgQueryService;
import trd.home.tcg.validator.CardSearchFilterValidator;

@Service
@RequiredArgsConstructor
public class TcgService {

    private final DeckEditService deckEdits;
    private final TcgCommandService commands;
    private final TcgQueryService queries;
    private final List<CardSearchFilterValidator> cardSearchFilterValidators;
    private final DeckExcelExportCommandService deckExcelExportService;
    private final DeckExcelImportService deckExcelImportService;
    private final DeckCsvImportService deckCsvImportService;
    private final DeckCsvExportCommandService deckCsvExportService;

    @LogMethodCall
    public DeckEditor getEditableDeck(String id) {
        return deckEdits.getDeck(id);
    }

    @LogMethodCall
    public void removeDeckCards(String id, String versionId, List<String> selected) {
        deckEdits.removeCards(id, versionId, selected);
    }

    @LogMethodCall
    public void addDeckCard(String id, String versionId, int quantity, String link, CardFoilType foilType) {
        deckEdits.addCard(id, versionId, quantity, link, foilType);
    }

    @LogMethodCall
    public void exportActiveDecksToCsv() {
        deckCsvExportService.exportDecks();
    }

    @LogMethodCall
    public void importDecksFromXlsx() {
        deckExcelImportService.importDecks();
    }

    @LogMethodCall
    public void exportActiveDecks() {
        deckExcelExportService.exportDecks();
    }

    @LogMethodCall
    public List<DeckVersionListItem> getVersionDecks() {
        return queries.getVersionDecks();
    }

    @LogMethodCall
    public DeckVersionHistory getDeckVersionHistory(String deckId) {
        return queries.getDeckVersionHistory(deckId);
    }

    @LogMethodCall
    @Cacheable(cacheNames = "tcg.card-foil-types")
    public CardFoilType[] getCardFoilTypes() {
        return CardFoilType.values();
    }

    @LogMethodCall
    @Cacheable(cacheNames = "tcg.card-game-types")
    public CardGameType[] getCardGameTypes() {
        return CardGameType.values();
    }

    @LogMethodCall
    @Cacheable(cacheNames = "tcg.card-price-types")
    public CardPriceType[] getCardPriceTypes() {
        return CardPriceType.values();
    }

    @LogMethodCall
    public Page<CardSearchResult> searchCards(
            CardSearchFilter filter, int page, int size, String sort, String direction) {
        cardSearchFilterValidators.forEach(validator -> validator.validate(filter, page, size));
        return queries.searchCards(
                filter,
                PageRequest.of(page, size, Sort.by(new Sort.Order(Sort.Direction.fromString(direction), sort))));
    }

    @LogMethodCall
    public void importDecksFromCsv() {
        deckCsvImportService.importDecks(null);
    }

    @LogMethodCall
    public void clearDecks() {
        commands.clearDecks();
    }

    @LogMethodCall
    public void importDeckFromCsv(String deckId) {
        deckCsvImportService.importDecks(deckId);
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
