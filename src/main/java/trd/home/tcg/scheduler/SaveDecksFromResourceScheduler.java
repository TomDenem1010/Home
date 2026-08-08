package trd.home.tcg.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.service.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

@Component
public class SaveDecksFromResourceScheduler {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketDeckSaver deckSaver;
    private final DeckFileReader deckFileReader;

    public SaveDecksFromResourceScheduler(
            ApplicationEventRepository eventRepository, CardmarketDeckSaver deckSaver, DeckFileReader deckFileReader) {
        this.eventRepository = eventRepository;
        this.deckSaver = deckSaver;
        this.deckFileReader = deckFileReader;
    }

    @Scheduled(fixedDelayString = "${tcg.scheduler.save-decks.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndProcessedAtIsNullAndErrorMessageIsNullOrderByCreatedAtAsc(
                        EventType.SAVE_DECKS_FROM_RESOURCE)
                .ifPresent(event -> {
                    try {
                        deckFileReader.read().forEach(deckSaver::save);
                        event.markProcessed();
                    } catch (RuntimeException exception) {
                        event.markFailed(exception);
                    }
                    eventRepository.save(event);
                });
    }
}
