package trd.home.tcg.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;

@Service
@RequiredArgsConstructor
public class DeckCsvImportService {
    private final ApplicationEventQueue events;
    private final FrontendNotificationPublisher notifications;

    public void importDecks(String deckId) {
        events.enqueue(EventType.IMPORT_DECKS_FROM_CSV, deckId);
        notifications.publish(FrontendNotificationType.WARNING, "CSV deck importing has started.");
    }
}
