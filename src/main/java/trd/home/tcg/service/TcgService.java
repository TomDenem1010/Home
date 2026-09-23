package trd.home.tcg.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.browser.ChromeBrowserLauncher;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.service.application.TcgCommandService;
import trd.home.tcg.service.application.TcgQueryService;

@Service
@RequiredArgsConstructor
public class TcgService {

    private final TcgCommandService commands;
    private final TcgQueryService queries;
    private final ChromeBrowserLauncher chromeBrowserLauncher;

    @LogMethodCall
    public void startChrome() {
        chromeBrowserLauncher.start();
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
    public void refreshDeckPrices() {
        commands.refreshDeckPrices(null);
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
