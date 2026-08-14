package trd.home.tcg.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.logging.LogMethodCall;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@AllArgsConstructor
public class TcgService {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketDeckRepository cardmarketDeckRepository;

    @LogMethodCall
    public void saveDecksFromResource() {
        eventRepository.save(new ApplicationEvent(EventType.SAVE_DECKS_FROM_RESOURCE));
    }

    @LogMethodCall
    public void refreshDeckPrices() {
        eventRepository.save(new ApplicationEvent(EventType.REFRESH_DECK_PRICES));
    }

    @LogMethodCall
    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        return cardmarketDeckRepository.calculateActiveDeckPriceSummaries();
    }

    @LogMethodCall
    public CardmarketDeckPriceHistorySummary getDeckPriceHistorySummary(String deckId) {
        return cardmarketDeckRepository.calculateDeckPriceHistorySummary(deckId);
    }
}
