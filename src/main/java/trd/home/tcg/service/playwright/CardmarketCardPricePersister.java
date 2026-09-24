package trd.home.tcg.service.playwright;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;

@Service
@AllArgsConstructor
public class CardmarketCardPricePersister {

    private final CardmarketCardRepository cardRepository;
    private final CardmarketCardPriceRepository priceRepository;

    @Transactional
    public void saveAll(List<GatheredCardmarketPrice> gatheredPrices) {
        List<CardmarketCardPrice> prices = gatheredPrices.stream()
                .map(this::removeZeroValues)
                .filter(gatheredPrice -> hasKnownPrice(gatheredPrice.price()))
                .map(gatheredPrice -> {
                    CardmarketCardPrice price = gatheredPrice.price();
                    price.setCard(cardRepository.getReferenceById(gatheredPrice.cardId()));
                    return price;
                })
                .toList();
        priceRepository.saveAll(prices);
    }

    private GatheredCardmarketPrice removeZeroValues(GatheredCardmarketPrice gatheredPrice) {
        CardmarketCardPrice price = gatheredPrice.price();
        if (isZero(price.getFromInEuro())) {
            price.setFromInEuro(null);
        }
        if (isZero(price.getTrendInEuro())) {
            price.setTrendInEuro(null);
        }
        return gatheredPrice;
    }

    private boolean hasKnownPrice(CardmarketCardPrice price) {
        return price.getFromInEuro() != null || price.getTrendInEuro() != null;
    }

    private boolean isZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }
}
