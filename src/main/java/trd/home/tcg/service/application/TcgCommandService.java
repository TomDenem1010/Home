package trd.home.tcg.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;

@Service
@RequiredArgsConstructor
public class TcgCommandService {

    private final ApplicationEventRepository eventRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public void saveDecksFromResource(String deckId) {
        createEvent(EventType.SAVE_DECKS_FROM_RESOURCE, deckId, "Deck saving has started.");
    }

    public void refreshDeckPrices(String deckId) {
        createEvent(EventType.REFRESH_DECK_PRICES, deckId, "Deck price refresh has started.");
    }

    private void createEvent(EventType eventType, String deckId, String notificationMessage) {
        eventRepository.save(new ApplicationEvent(eventType, deckId));
        notificationPublisher.publish(FrontendNotificationType.WARNING, notificationMessage);
    }
}
