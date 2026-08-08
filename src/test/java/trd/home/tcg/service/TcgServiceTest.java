package trd.home.tcg.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
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
}
