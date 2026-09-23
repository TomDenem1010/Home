package trd.home.tcg.service.event;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.exception.DeckImportException;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.deck.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

@Service
@RequiredArgsConstructor
public class SaveDecksFromResourceService {

    private static final String SUCCESS_MESSAGE = "Decks were saved successfully.";
    private static final String FAILURE_MESSAGE_PREFIX = "Failed to save decks: ";

    private final TcgEventProcessor eventProcessor;
    private final CardmarketDeckSaver deckSaver;
    private final DeckFileReader deckFileReader;
    private final CardmarketDeckRepository deckRepository;

    public void process(ApplicationEvent event) {
        eventProcessor.process(
                event, () -> saveDecks(selectedDecks(event.getMessage())), SUCCESS_MESSAGE, FAILURE_MESSAGE_PREFIX);
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
