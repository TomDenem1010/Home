package trd.home.tcg.service.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.exception.DeckPriceRefreshException;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

@Service
@RequiredArgsConstructor
public class RefreshDeckPricesService {

    private static final String SUCCESS_MESSAGE = "Deck prices were refreshed successfully.";
    private static final String FAILURE_MESSAGE_PREFIX = "Failed to refresh deck prices: ";

    private final ApplicationEventProcessor eventProcessor;
    private final CardmarketCardPriceSaver cardPriceSaver;
    private final CardmarketCardRepository cardRepository;
    private final CardmarketDeckRepository deckRepository;

    public void process(ApplicationEvent event) {
        eventProcessor.process(
                event,
                () -> {
                    refreshDecks(selectedDeckIds(event.getMessage()));
                    return SUCCESS_MESSAGE;
                },
                FAILURE_MESSAGE_PREFIX);
    }

    private List<String> selectedDeckIds(String deckId) {
        return deckId == null || deckId.isBlank() ? deckRepository.findIdsByStatus(DeckStatus.ACTIVE) : List.of(deckId);
    }

    private void refreshDecks(List<String> deckIds) {
        for (String deckId : deckIds) {
            try {
                cardPriceSaver.updateCardPrice(cardRepository.findAllInCurrentDeckVersionByDeckId(deckId).stream()
                        .map(trd.home.tcg.dto.CardmarketCardDto::from)
                        .toList());
            } catch (RuntimeException exception) {
                throw new DeckPriceRefreshException("Unable to refresh prices for deck: " + deckId, exception);
            }
        }
    }
}
