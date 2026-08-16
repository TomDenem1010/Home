package trd.home.tcg.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.service.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

@Component
public class SaveDecksFromResourceScheduler {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketDeckSaver deckSaver;
    private final DeckFileReader deckFileReader;
    private final FrontendNotificationPublisher notificationPublisher;

    public SaveDecksFromResourceScheduler(
            ApplicationEventRepository eventRepository,
            CardmarketDeckSaver deckSaver,
            DeckFileReader deckFileReader,
            FrontendNotificationPublisher notificationPublisher) {
        this.eventRepository = eventRepository;
        this.deckSaver = deckSaver;
        this.deckFileReader = deckFileReader;
        this.notificationPublisher = notificationPublisher;
    }

    @Scheduled(fixedDelayString = "${tcg.scheduler.save-decks.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO)
                .ifPresent(event -> {
                    event.markProcessing();
                    eventRepository.save(event);
                    try {
                        deckFileReader.read().forEach(deckSaver::save);
                        event.markDone();
                        notificationPublisher.publish(
                                FrontendNotificationType.SUCCESS, "A paklik mentése sikeresen befejeződött.");
                    } catch (RuntimeException exception) {
                        event.markFailed(exception);
                        notificationPublisher.publish(
                                FrontendNotificationType.ERROR,
                                "A paklik mentése sikertelen: " + exception.getMessage());
                    }
                    eventRepository.save(event);
                });
    }
}
