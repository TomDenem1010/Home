package trd.home.tcg.service.playwright;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;

@Service
@AllArgsConstructor
public class CardmarketCardPricePersister {

    private final CardmarketCardRepository cardRepository;
    private final CardmarketCardPriceRepository priceRepository;

    @Transactional
    @LogMethodCall
    public void saveAll(List<GatheredCardmarketPrice> gatheredPrices) {
        List<CardmarketCardPrice> prices = gatheredPrices.stream()
                .map(gatheredPrice -> {
                    CardmarketCardPrice price = gatheredPrice.price();
                    price.setCard(cardRepository.getReferenceById(gatheredPrice.cardId()));
                    return price;
                })
                .toList();
        priceRepository.saveAll(prices);
    }
}
