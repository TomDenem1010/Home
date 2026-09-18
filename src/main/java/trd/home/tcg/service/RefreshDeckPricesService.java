package trd.home.tcg.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.constant.DeckStatus;
import trd.home.tcg.exception.DeckPriceRefreshException;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshDeckPricesService {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketCardPriceSaver cardPriceSaver;
    private final CardmarketCardRepository cardRepository;
    private final CardmarketDeckRepository deckRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public void process(ApplicationEvent event) {
        event.markProcessing();
        eventRepository.save(event);
        FrontendNotificationType notificationType;
        String notificationMessage;
        try {
            refreshDecks(selectedDeckIds(event.getMessage()));
            event.markDone();
            notificationType = FrontendNotificationType.SUCCESS;
            notificationMessage = "Deck prices were refreshed successfully.";
        } catch (RuntimeException exception) {
            log.error("Failed to refresh Cardmarket prices for active decks", exception);
            event.markFailed(exception);
            notificationType = FrontendNotificationType.ERROR;
            notificationMessage = "Failed to refresh deck prices: " + exception.getMessage();
        }
        eventRepository.save(event);
        notificationPublisher.publish(event.getCreatedBy(), notificationType, notificationMessage);
    }

    private List<String> selectedDeckIds(String deckId) {
        return deckId == null || deckId.isBlank() ? deckRepository.findIdsByStatus(DeckStatus.ACTIVE) : List.of(deckId);
    }

    private void refreshDecks(List<String> deckIds) {
        for (String deckId : deckIds) {
            try {
                cardPriceSaver.updateCardPrice(cardRepository.findAllInDeckCurrentVersion(deckId));
            } catch (RuntimeException exception) {
                throw new DeckPriceRefreshException("Unable to refresh prices for deck: " + deckId, exception);
            }
        }
    }
}
