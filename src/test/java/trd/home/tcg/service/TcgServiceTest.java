package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.common.browser.ChromeBrowserLauncher;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.service.application.TcgCommandService;
import trd.home.tcg.service.application.TcgQueryService;

class TcgServiceTest {

    private final TcgCommandService commands = mock(TcgCommandService.class);
    private final TcgQueryService queries = mock(TcgQueryService.class);
    private final ChromeBrowserLauncher chromeBrowserLauncher = mock(ChromeBrowserLauncher.class);
    private final TcgService service = new TcgService(commands, queries, chromeBrowserLauncher);

    @Test
    void startsChrome() {
        service.startChrome();

        verify(chromeBrowserLauncher).start();
    }

    @Test
    void createsSaveDecksFromResourceEvent() {
        service.saveDecksFromResource();
        verify(commands).saveDecksFromResource(null);
    }

    @Test
    void createsRefreshDeckPricesEvent() {
        service.refreshDeckPrices();
        verify(commands).refreshDeckPrices(null);
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
