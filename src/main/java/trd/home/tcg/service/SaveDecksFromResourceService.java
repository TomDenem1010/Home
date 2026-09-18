package trd.home.tcg.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.FrontendNotificationPublisher;
import trd.home.common.event.FrontendNotificationType;
import trd.home.common.repository.ApplicationEventRepository;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.exception.DeckImportException;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.file.DeckFileReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveDecksFromResourceService {

    private final ApplicationEventRepository eventRepository;
    private final CardmarketDeckSaver deckSaver;
    private final DeckFileReader deckFileReader;
    private final CardmarketDeckRepository deckRepository;
    private final FrontendNotificationPublisher notificationPublisher;

    public void process(ApplicationEvent event) {
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
    }

    private List<CardmarketDeck> selectedDecks(String deckId) {
        List<CardmarketDeck> decks = deckFileReader.read();
        if (deckId == null || deckId.isBlank()) {
            return decks;
        }

        String deckName = deckRepository
                .findById(deckId)
                .orElseThrow(() -> new DeckImportException("Deck not found: " + deckId))
                .getName();
        List<CardmarketDeck> selectedDecks =
                decks.stream().filter(deck -> deck.getName().equals(deckName)).toList();
        if (selectedDecks.isEmpty()) {
            throw new DeckImportException("No resource file found for deck: " + deckName);
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
            throw new DeckImportException("Unable to save decks from resource", exception.getCause());
        }
    }
}
