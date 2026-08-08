package trd.home.frontend.tcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.service.TcgService;

class TcgFrontendControllerTest {

    private final TcgService tcgService = mock(TcgService.class);
    private final TcgFrontendController controller = new TcgFrontendController(tcgService);

    @Test
    void tcgReturnsIndexAndMarksTcgAsActive() {
        var model = new ConcurrentModel();

        assertEquals("index", controller.tcg(model));
        assertEquals("/tcg", model.getAttribute("activePath"));
    }

    @Test
    void saveDecksFromResourceCallsTcgService() {
        assertEquals("redirect:/tcg", controller.saveDecksFromResource());

        verify(tcgService).saveDecksFromResource();
    }

    @Test
    void refreshDeckPricesCallsTcgService() {
        assertEquals("redirect:/tcg", controller.refreshDeckPrices());

        verify(tcgService).refreshDeckPrices();
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
