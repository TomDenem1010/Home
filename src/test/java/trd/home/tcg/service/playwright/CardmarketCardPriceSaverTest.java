package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import trd.home.common.playwright.PlaywrightBrowserSession;
import trd.home.common.playwright.PlaywrightBrowserSessionFactory;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardLanguage;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dto.CardmarketCardDto;

class CardmarketCardPriceSaverTest {

    private final CardmarketCardPriceGatherer gatherer = mock(CardmarketCardPriceGatherer.class);
    private final CardmarketRequestThrottler throttler = mock(CardmarketRequestThrottler.class);
    private final CardmarketCardPricePersister persister = mock(CardmarketCardPricePersister.class);
    private final PlaywrightBrowserSessionFactory sessionFactory = mock(PlaywrightBrowserSessionFactory.class);
    private final PlaywrightBrowserSession session = mock(PlaywrightBrowserSession.class);
    private final Browser browser = mock(Browser.class);
    private final CardmarketCardPriceSaver saver =
            new CardmarketCardPriceSaver(gatherer, throttler, persister, sessionFactory, "browser-endpoint");

    @BeforeEach
    void setUp() {
        when(sessionFactory.open("browser-endpoint")).thenReturn(session);
        when(session.getBrowser()).thenReturn(browser);
    }

    @Test
    void persistsEachPriceImmediatelyAfterGatheringIt() {
        CardmarketCardDto firstCard = card("card-1");
        CardmarketCardDto secondCard = card("card-2");
        CardmarketCardPrice firstPrice = new CardmarketCardPrice();
        CardmarketCardPrice secondPrice = new CardmarketCardPrice();
        when(gatherer.getCardmarketCardPrice(firstCard.link(), browser)).thenReturn(firstPrice);
        when(gatherer.getCardmarketCardPrice(secondCard.link(), browser)).thenReturn(secondPrice);

        saver.updateCardPrice(List.of(firstCard, secondCard));

        InOrder order = inOrder(gatherer, throttler, persister);
        order.verify(gatherer).getCardmarketCardPrice(firstCard.link(), browser);
        order.verify(persister).saveAll(List.of(new GatheredCardmarketPrice("card-1", firstPrice)));
        order.verify(throttler).waitBeforeNextRequest();
        order.verify(gatherer).getCardmarketCardPrice(secondCard.link(), browser);
        order.verify(persister).saveAll(List.of(new GatheredCardmarketPrice("card-2", secondPrice)));
        verify(session).close();
    }

    @Test
    void keepsPricesPersistedBeforeGatheringFails() {
        CardmarketCardDto firstCard = card("card-1");
        CardmarketCardDto secondCard = card("card-2");
        CardmarketCardPrice firstPrice = new CardmarketCardPrice();
        when(gatherer.getCardmarketCardPrice(firstCard.link(), browser)).thenReturn(firstPrice);
        when(gatherer.getCardmarketCardPrice(secondCard.link(), browser))
                .thenThrow(new IllegalStateException("Rate limited"));

        assertThrows(IllegalStateException.class, () -> saver.updateCardPrice(List.of(firstCard, secondCard)));

        verify(persister).saveAll(List.of(new GatheredCardmarketPrice("card-1", firstPrice)));
        verify(session).close();
    }

    @Test
    void doesNotThrottleBeforeTheFirstCard() {
        CardmarketCardDto card = card("card-1");
        when(gatherer.getCardmarketCardPrice(card.link(), browser)).thenReturn(new CardmarketCardPrice());

        assertDoesNotThrow(() -> saver.updateCardPrice(List.of(card)));

        verify(throttler, never()).waitBeforeNextRequest();
        verify(gatherer, times(1)).getCardmarketCardPrice(card.link(), browser);
    }

    private static CardmarketCardDto card(String id) {
        return new CardmarketCardDto(
                id,
                "https://example.test/" + id,
                CardFoilType.NO,
                CardGameType.MAGIC_THE_GATHERING,
                "",
                "",
                CardLanguage.ENGLISH);
    }
}
