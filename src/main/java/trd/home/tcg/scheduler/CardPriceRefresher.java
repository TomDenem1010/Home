package trd.home.tcg.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import trd.home.tcg.repository.CardmarketCardRepository;
import trd.home.tcg.service.playwright.CardmarketCardPriceSaver;

@Slf4j
@Service
@AllArgsConstructor
public class CardPriceRefresher {

    private final CardmarketCardRepository cardmarketCardRepository;
    private final CardmarketCardPriceSaver cardmarketCardPriceSaver;

    @Scheduled(fixedDelay = 3600000, initialDelay = 30000)
    public void refreshDecks() {
        try {
            cardmarketCardPriceSaver.updateCardPrice(cardmarketCardRepository.findAllInActiveDeckCurrentVersions());
        } catch (Exception e) {
            log.error("Error occurred while refreshing card prices: {}", e.getMessage(), e);
        }
    }
}
