package trd.home.tcg.scheduler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

@Slf4j
@Component
public class SaveDecksFromResourceScheduler {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketDeckSaver deckSaver;
    private final DeckFileReader deckFileReader;
    private final CardmarketDeckRepository deckRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public SaveDecksFromResourceScheduler(
            ApplicationEventRepository eventRepository,
            CardmarketDeckSaver deckSaver,
            DeckFileReader deckFileReader,
            CardmarketDeckRepository deckRepository,
            FrontendNotificationPublisher notificationPublisher) {
        this.eventRepository = eventRepository;
        this.deckSaver = deckSaver;
        this.deckFileReader = deckFileReader;
        this.deckRepository = deckRepository;
        this.notificationPublisher = notificationPublisher;
    }

    @Scheduled(fixedDelayString = "${tcg.scheduler.save-decks.delay:5s}")
    public void processNextEvent() {
        eventRepository
                .findFirstByTypeAndStatusOrderByCreatedAtAsc(EventType.SAVE_DECKS_FROM_RESOURCE, EventStatus.TO_DO)
                .ifPresent(event -> {
                    event.markProcessing();
                    eventRepository.save(event);
                    FrontendNotificationType notificationType;
                    String notificationMessage;
                    try {
                        saveDecks(selectedDecks(event.getMessage()));
                        event.markDone();
                        notificationType = FrontendNotificationType.SUCCESS;
                        notificationMessage = "Decks were saved successfully.";
                    } catch (RuntimeException exception) {
                        log.error("Failed to save decks from configured resource files", exception);
                        event.markFailed(exception);
                        notificationType = FrontendNotificationType.ERROR;
                        notificationMessage = "Failed to save decks: " + exception.getMessage();
                    }
                    eventRepository.save(event);
                    notificationPublisher.publish(event.getCreatedBy(), notificationType, notificationMessage);
                });
        log.info("Finished processing save decks from resource event");
    }

    private List<CardmarketDeck> selectedDecks(String deckId) {
        List<CardmarketDeck> decks = deckFileReader.read();
        if (deckId == null || deckId.isBlank()) {
            return decks;
        }

        String deckName = deckRepository.findById(deckId).orElseThrow().getName();
        List<CardmarketDeck> selectedDecks =
                decks.stream().filter(deck -> deck.getName().equals(deckName)).toList();
        if (selectedDecks.isEmpty()) {
            throw new IllegalArgumentException("No resource file found for deck: " + deckName);
        }
        return selectedDecks;
    }

    private void saveDecks(List<CardmarketDeck> decks) {
        try {
            CompletableFuture.allOf(decks.stream()
                            .map(deck -> CompletableFuture.runAsync(() -> deckSaver.save(deck)))
                            .toArray(CompletableFuture[]::new))
                    .join();
        } catch (CompletionException exception) {
            throw (RuntimeException) exception.getCause();
        }
    }
}
