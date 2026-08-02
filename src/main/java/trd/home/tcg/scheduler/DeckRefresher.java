package trd.home.tcg.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import trd.home.tcg.service.CardmarketDeckSaver;
import trd.home.tcg.service.file.DeckFileReader;

@Slf4j
@Service
@AllArgsConstructor
public class DeckRefresher {

    private final DeckFileReader deckFileReader;
    private final CardmarketDeckSaver deckSaver;

    @Scheduled(fixedDelay = 3600000, initialDelay = 10000)
    public void refreshDecks() {
        try {
            deckFileReader.read().forEach(deckSaver::save);
        } catch (Exception e) {
            log.error("Error occurred while refreshing decks: {}", e.getMessage(), e);
        }
    }
}
