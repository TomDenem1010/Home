package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.dto.CardSearchResult;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.service.application.TcgCommandService;
import trd.home.tcg.service.application.TcgQueryService;

class TcgServiceTest {

    private final TcgCommandService commands = mock(TcgCommandService.class);
    private final TcgQueryService queries = mock(TcgQueryService.class);
    private final TcgService service = new TcgService(commands, queries);

    @Test
    void providesEnumValuesForSearchForm() {
        assertArrayEquals(CardFoilType.values(), service.getCardFoilTypes());
        assertArrayEquals(CardGameType.values(), service.getCardGameTypes());
        assertArrayEquals(trd.home.tcg.constant.CardPriceType.values(), service.getCardPriceTypes());
    }

    @Test
    void passesSearchAndPagingToQueryServiceWithoutValidatingPriceRange() {
        var filter = new CardSearchFilter();
        filter.setPriceMin(new java.math.BigDecimal("10"));
        filter.setPriceMax(new java.math.BigDecimal("-5"));
        var pageable = PageRequest.of(2, 25, Sort.by(Sort.Direction.DESC, "quantity"));
        var results = new PageImpl<CardSearchResult>(List.of());
        when(queries.searchCards(filter, pageable)).thenReturn(results);
        assertSame(results, service.searchCards(filter, 2, 25, "quantity", "desc"));
        verify(queries).searchCards(filter, pageable);
    }

    @Test
    void createsSaveDecksFromResourceEvent() {
        service.saveDecksFromResource();
        verify(commands).saveDecksFromResource(null);
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
