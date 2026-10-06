package trd.home.tcg.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.service.deck.CardmarketDeckClearer;

@Service
@RequiredArgsConstructor
public class ClearDecksService {
    private final ApplicationEventProcessor eventProcessor;
    private final CardmarketDeckClearer deckClearer;

    public void process(ApplicationEvent event) {
        eventProcessor.process(
                event,
                () -> {
                    deckClearer.clear();
                    return "Decks were cleared successfully.";
                },
                "Failed to clear decks: ");
    }
}
