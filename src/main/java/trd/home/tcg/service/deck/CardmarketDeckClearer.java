package trd.home.tcg.service.deck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.tcg.repository.CardmarketDeckCardRepository;
import trd.home.tcg.repository.CardmarketDeckRepository;
import trd.home.tcg.repository.CardmarketDeckVersionRepository;

@Service
@RequiredArgsConstructor
public class CardmarketDeckClearer {
    private final CardmarketDeckRepository deckRepository;
    private final CardmarketDeckVersionRepository deckVersionRepository;
    private final CardmarketDeckCardRepository deckCardRepository;

    @Transactional
    public void clear() {
        var decks = deckRepository.findAll();
        decks.forEach(deck -> deck.setCurrentVersion(null));
        deckRepository.saveAllAndFlush(decks);
        deckCardRepository.deleteAllInBatch();
        deckVersionRepository.deleteAllInBatch();
        deckRepository.deleteAllInBatch();
    }
}
