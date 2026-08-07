package trd.home.tcg.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.service.file.DeckFileReader;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

@Service
@AllArgsConstructor
public class TcgService {

    private final CardmarketCardPriceSaver cardmarketCardPriceSaver;
    private final CardmarketCardRepository cardmarketCardRepository;
    private final CardmarketDeckRepository cardmarketDeckRepository;
    private final CardmarketDeckSaver deckSaver;
    private final DeckFileReader deckFileReader;

    @Async
    public void saveDecksFromResource() {
        deckFileReader.read().forEach(deckSaver::save);
    }

    @Async
    public void refreshDeckPrices() {
        cardmarketCardPriceSaver.updateCardPrice(cardmarketCardRepository.findAllInActiveDeckCurrentVersions());
    }

    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        return cardmarketDeckRepository.calculateActiveDeckPriceSummaries();
    }
}
