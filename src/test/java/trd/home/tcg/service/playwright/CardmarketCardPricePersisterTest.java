package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;

class CardmarketCardPricePersisterTest {

    private final CardmarketCardRepository cardRepository = mock(CardmarketCardRepository.class);
    private final CardmarketCardPriceRepository priceRepository = mock(CardmarketCardPriceRepository.class);
    private final CardmarketCardPricePersister persister =
            new CardmarketCardPricePersister(cardRepository, priceRepository);

    @Test
    void associatesCardsAndSavesAllPrices() {
        CardmarketCard card = new CardmarketCard();
        CardmarketCardPrice price = new CardmarketCardPrice();
        price.setFromInEuro(BigDecimal.ONE);
        when(cardRepository.getReferenceById("card-id")).thenReturn(card);

        persister.saveAll(List.of(new GatheredCardmarketPrice("card-id", price)));

        assertSame(card, price.getCard());
        verify(priceRepository).saveAll(List.of(price));
    }

    @Test
    void doesNotSaveAnUnknownZeroPrice() {
        CardmarketCardPrice price = new CardmarketCardPrice();
        price.setFromInEuro(BigDecimal.ZERO);
        price.setTrendInEuro(new BigDecimal("0.0000"));

        persister.saveAll(List.of(new GatheredCardmarketPrice("card-id", price)));

        assertNull(price.getFromInEuro());
        assertNull(price.getTrendInEuro());
        verify(cardRepository, never()).getReferenceById("card-id");
        verify(priceRepository).saveAll(List.of());
    }

    @Test
    void storesKnownPriceWithoutAZeroPlaceholder() {
        CardmarketCard card = new CardmarketCard();
        CardmarketCardPrice price = new CardmarketCardPrice();
        price.setFromInEuro(BigDecimal.ZERO);
        price.setTrendInEuro(new BigDecimal("2.50"));
        when(cardRepository.getReferenceById("card-id")).thenReturn(card);

        persister.saveAll(List.of(new GatheredCardmarketPrice("card-id", price)));

        assertNull(price.getFromInEuro());
        assertSame(card, price.getCard());
        verify(priceRepository).saveAll(List.of(price));
    }
}
