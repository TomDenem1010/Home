package trd.home.tcg.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.constant.EventType;
import trd.home.common.event.ApplicationEventQueue;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;

@Service
@RequiredArgsConstructor
public class DeckExcelExportCommandService {
    private final ApplicationEventQueue events;
    private final FrontendNotificationPublisher notifications;

    public void exportDecks() {
        events.enqueue(EventType.EXPORT_DECKS_TO_XLSX, null);
        notifications.publish(FrontendNotificationType.WARNING, "XLSX deck exporting has started.");
    }
}
