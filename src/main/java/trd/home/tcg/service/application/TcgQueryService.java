package trd.home.tcg.service.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.tcg.dto.CardmarketDeckPriceHistorySummary;
import trd.home.tcg.dto.CardmarketDeckPriceSummary;
import trd.home.tcg.repository.CardmarketDeckRepository;

@Service
@RequiredArgsConstructor
public class TcgQueryService {

    private final CardmarketDeckRepository deckRepository;

    public List<CardmarketDeckPriceSummary> getDeckPriceSummary() {
        return deckRepository.calculateActiveDeckPriceSummaries();
    }

    public CardmarketDeckPriceHistorySummary getDeckPriceHistorySummary(String deckId) {
        return deckRepository.calculateDeckPriceHistorySummary(deckId);
    }
}
