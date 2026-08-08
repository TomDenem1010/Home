package trd.home.tcg.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketDeckRepository;

class TcgServiceTest {

    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final CardmarketDeckRepository deckRepository = mock(CardmarketDeckRepository.class);
    private final TcgService service = new TcgService(eventRepository, deckRepository);

    @Test
    void createsSaveDecksFromResourceEvent() {
        service.saveDecksFromResource();

        verify(eventRepository).save(argThat(event -> event.getType() == EventType.SAVE_DECKS_FROM_RESOURCE));
    }

    @Test
    void createsRefreshDeckPricesEvent() {
        service.refreshDeckPrices();

        verify(eventRepository).save(argThat(event -> event.getType() == EventType.REFRESH_DECK_PRICES));
    }

    @Test
    void returnsDeckPriceSummaries() {
        List<CardmarketDeckPriceSummary> summaries = List.of(mock(CardmarketDeckPriceSummary.class));
        when(deckRepository.calculateActiveDeckPriceSummaries()).thenReturn(summaries);

        assertSame(summaries, service.getDeckPriceSummary());
    }

    @Test
    void returnsDeckPriceHistorySummary() {
        CardmarketDeckPriceHistorySummary summary = mock(CardmarketDeckPriceHistorySummary.class);
        when(deckRepository.calculateDeckPriceHistorySummary("deck-id")).thenReturn(summary);

        assertSame(summary, service.getDeckPriceHistorySummary("deck-id"));
    }
}
