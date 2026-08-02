package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;

@ExtendWith(MockitoExtension.class)
class CardmarketCardPriceSaverTest {

    @Mock
    private CardmarketCardPriceGatherer gatherer;

    @Mock
    private CardmarketCardRepository cardRepository;

    @Mock
    private CardmarketCardPriceRepository priceRepository;

    @Mock
    private Browser browser;

    @InjectMocks
    private CardmarketCardPriceSaver saver;

    @Test
    void associatesAndSavesEachGatheredPrice() {
        CardmarketCardDto cardDto =
                new CardmarketCardDto("card-1", "https://example.test/card", null, null, null, null, null);
        CardmarketCard card = new CardmarketCard();
        CardmarketCardPrice price = new CardmarketCardPrice();
        price.setFromInEuro(BigDecimal.ONE);
        when(gatherer.getCardmarketCardPrice(cardDto.link(), browser)).thenReturn(price);
        when(cardRepository.getReferenceById(cardDto.id())).thenReturn(card);

        try (MockedConstruction<PlaywrightBrowserContext> contexts =
                mockConstruction(PlaywrightBrowserContext.class, (mock, ignored) -> when(mock.getBrowser())
                        .thenReturn(browser))) {
            saver.updateCardPrice(List.of(cardDto));

            assertSame(card, price.getCard());
            verify(priceRepository).save(price);
            verify(contexts.constructed().getFirst()).close();
        }
    }
}
