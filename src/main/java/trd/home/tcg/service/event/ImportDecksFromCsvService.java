package trd.home.tcg.service.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.event.ApplicationEventProcessor;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.exception.DeckImportException;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.deck.DeckCsvSaver;
import trd.home.tcg.service.file.DeckCsvFileReader;

@Service
@RequiredArgsConstructor
public class ImportDecksFromCsvService {

    private static final String SUCCESS_MESSAGE = "CSV deck importing completed successfully.";
    private static final String FAILURE_MESSAGE_PREFIX = "Failed to import CSV decks: ";

    private final ApplicationEventProcessor eventProcessor;
    private final DeckCsvSaver deckSaver;
    private final DeckCsvFileReader deckCsvFileReader;
    private final CardmarketDeckRepository deckRepository;

    public void process(ApplicationEvent event) {
        eventProcessor.process(
                event,
                () -> {
                    importDecks(selectedDecks(event.getMessage()));
                    return SUCCESS_MESSAGE;
                },
                FAILURE_MESSAGE_PREFIX);
    }

    private List<CardmarketDeck> selectedDecks(String deckId) {
        List<CardmarketDeck> decks = deckCsvFileReader.read();
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
            throw new DeckImportException("No CSV file found for deck: " + deckName);
        }
        return selectedDecks;
    }

    private void importDecks(List<CardmarketDeck> decks) {
        try {
            for (CardmarketDeck deck : decks) {
                deckSaver.save(deck);
            }
        } catch (RuntimeException exception) {
            throw new DeckImportException("Unable to import decks from CSV", exception);
        }
    }
}
