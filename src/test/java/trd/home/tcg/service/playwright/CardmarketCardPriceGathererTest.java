package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import java.math.BigDecimal;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trd.home.tcg.dao.CardmarketCardPrice;

@ExtendWith(MockitoExtension.class)
class CardmarketCardPriceGathererTest {

    @Mock
    private CardmarketCaller caller;

    @Mock
    private Browser browser;

    @Test
    void extractsEuroPricesFromCardmarketPage() {
        when(caller.callWithPlaywright("https://example.test/card", browser)).thenReturn(Jsoup.parse("""
                        <dl>
                          <dt>From</dt><dd>1.234,56 €</dd>
                          <dt>Price Trend</dt><dd>987,65 €</dd>
                        </dl>
                        """));

        CardmarketCardPrice price =
                new CardmarketCardPriceGatherer(caller).getCardmarketCardPrice("https://example.test/card", browser);

        assertEquals(new BigDecimal("1234.56"), price.getFromInEuro());
        assertEquals(new BigDecimal("987.65"), price.getTrendInEuro());
    }

    @Test
    void usesZeroForMissingPrices() {
        when(caller.callWithPlaywright("https://example.test/card", browser)).thenReturn(Jsoup.parse("<dl />"));

        CardmarketCardPrice price =
                new CardmarketCardPriceGatherer(caller).getCardmarketCardPrice("https://example.test/card", browser);

        assertEquals(BigDecimal.ZERO, price.getFromInEuro());
        assertEquals(BigDecimal.ZERO, price.getTrendInEuro());
    }
}
