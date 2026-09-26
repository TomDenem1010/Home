package trd.home.frontend.tcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ConcurrentModel;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.service.TcgService;

class TcgFrontendControllerTest {

    private final TcgService tcgService = mock(TcgService.class);
    private final TcgFrontendController controller = new TcgFrontendController(tcgService, new FrontendPageRenderer());

    @Test
    void searchPageGetsEnumsThroughTcgServiceAndAssemblesModel() {
        var model = new ConcurrentModel();
        var filter = new CardSearchFilter();
        when(tcgService.getCardFoilTypes()).thenReturn(CardFoilType.values());
        when(tcgService.getCardGameTypes()).thenReturn(CardGameType.values());
        when(tcgService.getCardPriceTypes()).thenReturn(trd.home.tcg.constant.CardPriceType.values());
        assertEquals("index", controller.searchCard(filter, 0, 50, "name", "asc", model));
        assertEquals("tcg/search-card", model.getAttribute("contentTemplate"));
        assertEquals(50, model.getAttribute("size"));
        assertEquals("name", model.getAttribute("sort"));
        assertEquals("asc", model.getAttribute("direction"));
        assertEquals(List.of(CardFoilType.values()), List.of((CardFoilType[]) model.getAttribute("foilTypes")));
        assertEquals(List.of(CardGameType.values()), List.of((CardGameType[]) model.getAttribute("cardGameTypes")));
        verify(tcgService).getCardFoilTypes();
        verify(tcgService).getCardGameTypes();
        verify(tcgService).getCardPriceTypes();
        verify(tcgService).searchCards(filter, 0, 50, "name", "asc");
        org.mockito.Mockito.verifyNoMoreInteractions(tcgService);
    }

    @Test
    void searchPassesFiltersAndPagingParametersThroughTcgService() {
        var filter = new CardSearchFilter();
        var model = new ConcurrentModel();
        var results = new PageImpl<trd.home.tcg.dto.CardSearchResult>(List.of());
        when(tcgService.searchCards(filter, 2, 25, "quantity", "desc")).thenReturn(results);
        controller.searchCard(filter, 2, 25, "quantity", "desc", model);
        assertEquals(results, model.getAttribute("searchResults"));
        assertEquals(25, model.getAttribute("size"));
        assertEquals("quantity", model.getAttribute("sort"));
        assertEquals("desc", model.getAttribute("direction"));
        verify(tcgService).searchCards(filter, 2, 25, "quantity", "desc");
    }

    @Test
    void searchForwardsPriceRangeWithoutValidation() {
        var filter = new CardSearchFilter();
        filter.setPriceMin(new BigDecimal("10"));
        filter.setPriceMax(new BigDecimal("-5"));
        controller.searchCard(filter, 0, 50, "name", "asc", new ConcurrentModel());
        verify(tcgService).searchCards(filter, 0, 50, "name", "asc");
    }

    @Test
    void tcgReturnsIndexAndMarksTcgAsActive() {
        var model = new ConcurrentModel();

        assertEquals("index", controller.tcg(model));
        assertEquals("/tcg", model.getAttribute("activePath"));
        assertEquals("/css/tcg.css", model.getAttribute("featureStylesheet"));
        assertEquals("/js/tcg.js", model.getAttribute("featureScript"));
    }

    @Test
    void saveDecksFromResourceCallsTcgService() {
        assertEquals("redirect:/tcg", controller.saveDecksFromResource());

        verify(tcgService).saveDecksFromResource();
    }

    @Test
    void reloadDeckCreatesEventForSelectedDeck() {
        assertEquals("redirect:/tcg/statistics", controller.reloadDeck("deck-id"));

        verify(tcgService).saveDeckFromResource("deck-id");
    }

    @Test
    void refreshDeckPricesCreatesEventForSelectedDeck() {
        assertEquals("redirect:/tcg/statistics", controller.refreshDeckPrices("deck-id"));

        verify(tcgService).refreshDeckPrices("deck-id");
    }

    @Test
    void statisticsReturnsDeckPriceSummaries() {
        var model = new ConcurrentModel();
        var summaries = List.of(new CardmarketDeckPriceSummary(
                "deck-id", "Test deck", new BigDecimal("12.34"), new BigDecimal("23.45")));
        when(tcgService.getDeckPriceSummary()).thenReturn(summaries);

        assertEquals("index", controller.statistics(model));
        assertEquals("Statistics", model.getAttribute("pageTitle"));
        assertEquals(summaries, model.getAttribute("deckPriceSummaries"));
        assertEquals("/tcg/statistics", model.getAttribute("activePath"));
        assertEquals("tcg/statistics", model.getAttribute("contentTemplate"));
        verify(tcgService).getDeckPriceSummary();
    }

    @Test
    void deckPriceHistoryReturnsSummaryForDeckId() {
        var model = new ConcurrentModel();
        var summary = new CardmarketDeckPriceHistorySummary("deck-id", List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        when(tcgService.getDeckPriceHistorySummary("deck-id")).thenReturn(summary);

        assertEquals("index", controller.deckPriceHistory("deck-id", model));
        assertEquals("Deck price history", model.getAttribute("pageTitle"));
        assertEquals(summary, model.getAttribute("deckPriceHistorySummary"));
        assertEquals("/tcg/statistics", model.getAttribute("activePath"));
        assertEquals("tcg/statistics-uuid", model.getAttribute("contentTemplate"));
        verify(tcgService).getDeckPriceHistorySummary("deck-id");
    }
}
