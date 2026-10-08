package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardPriceType;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.dto.CardSearchResult;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.dto.DeckExcelData;
import trd.home.tcg.exception.InvalidCardSearchFilterException;
import trd.home.tcg.service.application.TcgCommandService;
import trd.home.tcg.service.application.TcgQueryService;
import trd.home.tcg.service.excel.DeckExcelExportService;
import trd.home.tcg.validator.CardSearchFilterValidator;
import trd.home.tcg.validator.CardSearchPagingValidator;
import trd.home.tcg.validator.CardSearchPriceValidator;

class TcgServiceTest {

    private final TcgCommandService commands = mock(TcgCommandService.class);
    private final TcgQueryService queries = mock(TcgQueryService.class);
    private final CardSearchFilterValidator validator = mock(CardSearchFilterValidator.class);
    private final DeckExcelExportService exporter = mock(DeckExcelExportService.class);
    private final TcgService service = new TcgService(commands, queries, List.of(validator), exporter);

    @Test
    void returnsVersionDecksAndSelectedDeckHistory() {
        var decks = List.of(new trd.home.tcg.dto.DeckVersionListItem("id", "Deck"));
        var history = new trd.home.tcg.dto.DeckVersionHistory("Deck", List.of());
        when(queries.getVersionDecks()).thenReturn(decks);
        when(queries.getDeckVersionHistory("id")).thenReturn(history);
        assertSame(decks, service.getVersionDecks());
        assertSame(history, service.getDeckVersionHistory("id"));
        verify(queries).getDeckVersionHistory("id");
    }

    @Test
    void exportsActiveDeckDataThroughExcelService() {
        var decks = List.of(new DeckExcelData("Deck", "v1", List.of()));
        byte[] content = {1, 2, 3};
        when(queries.getDeckExcelData()).thenReturn(decks);
        when(exporter.export(decks)).thenReturn(content);
        assertSame(content, service.exportActiveDecks());
        var order = inOrder(queries, exporter);
        order.verify(queries).getDeckExcelData();
        order.verify(exporter).export(decks);
    }

    @Test
    void providesEnumValuesForSearchForm() {
        assertArrayEquals(CardFoilType.values(), service.getCardFoilTypes());
        assertArrayEquals(CardGameType.values(), service.getCardGameTypes());
        assertArrayEquals(CardPriceType.values(), service.getCardPriceTypes());
    }

    @Test
    void validatesFilterBeforePassingSearchAndPagingToQueryService() {
        var filter = new CardSearchFilter(null, null, null, null, null, null);
        var pageable = PageRequest.of(2, 25, Sort.by(new Sort.Order(Sort.Direction.DESC, "quantity")));
        var results = new PageImpl<CardSearchResult>(List.of());
        when(queries.searchCards(filter, pageable)).thenReturn(results);
        assertSame(results, service.searchCards(filter, 2, 25, "quantity", "desc"));
        var order = inOrder(validator, queries);
        order.verify(validator).validate(filter, 2, 25);
        order.verify(queries).searchCards(filter, pageable);
    }

    @Test
    void invalidFilterPreventsQuery() {
        var validatingService = new TcgService(commands, queries, List.of(new CardSearchPriceValidator()), exporter);
        var filter = new CardSearchFilter(null, null, null, new BigDecimal("-1"), null, null);
        assertThrows(
                InvalidCardSearchFilterException.class,
                () -> validatingService.searchCards(filter, 0, 50, "name", "asc"));
        verifyNoInteractions(queries);
    }

    @Test
    void invalidPagingPreventsQuery() {
        var validatingService = new TcgService(
                commands, queries, List.of(new CardSearchPriceValidator(), new CardSearchPagingValidator()), exporter);
        var filter = new CardSearchFilter(null, null, null, null, null, null);
        for (int[] paging : List.of(new int[] {-1, 50}, new int[] {0, 0}, new int[] {0, 201})) {
            assertThrows(
                    InvalidCardSearchFilterException.class,
                    () -> validatingService.searchCards(filter, paging[0], paging[1], "name", "asc"));
        }
        verifyNoInteractions(queries);
    }

    @Test
    void createsSaveDecksFromResourceEvent() {
        service.saveDecksFromResource();
        verify(commands).saveDecksFromResource(null);
    }

    @Test
    void createsClearDecksEvent() {
        service.clearDecks();
        verify(commands).clearDecks();
    }

    @Test
    void createsSaveDeckEventForSelectedDeck() {
        service.saveDeckFromResource("deck-id");

        verify(commands).saveDecksFromResource("deck-id");
    }

    @Test
    void createsRefreshPriceEventForSelectedDeck() {
        service.refreshDeckPrices("deck-id");

        verify(commands).refreshDeckPrices("deck-id");
    }

    @Test
    void returnsDeckPriceSummaries() {
        List<CardmarketDeckPriceSummary> summaries = List.of(mock(CardmarketDeckPriceSummary.class));
        when(queries.getDeckPriceSummary()).thenReturn(summaries);

        assertSame(summaries, service.getDeckPriceSummary());
    }

    @Test
    void returnsDeckPriceHistorySummary() {
        CardmarketDeckPriceHistorySummary summary = mock(CardmarketDeckPriceHistorySummary.class);
        when(queries.getDeckPriceHistorySummary("deck-id")).thenReturn(summary);

        assertSame(summary, service.getDeckPriceHistorySummary("deck-id"));
    }
}
