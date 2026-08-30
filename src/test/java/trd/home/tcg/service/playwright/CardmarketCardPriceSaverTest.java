package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dto.CardmarketCardDto;

@ExtendWith(MockitoExtension.class)
class CardmarketCardPriceSaverTest {

    @Mock
    private CardmarketCardPriceGatherer gatherer;

    @Mock
    private CardmarketRequestThrottler throttler;

    @Mock
    private CardmarketCardPricePersister persister;

    @Mock
    private Browser browser;

    @InjectMocks
    private CardmarketCardPriceSaver saver;

    @Test
    @SuppressWarnings("unchecked")
    void gathersEveryPriceBeforePersistingThemTogether() {
        CardmarketCardDto firstCard = card("card-1");
        CardmarketCardDto secondCard = card("card-2");
        CardmarketCardPrice firstPrice = new CardmarketCardPrice();
        CardmarketCardPrice secondPrice = new CardmarketCardPrice();
        when(gatherer.getCardmarketCardPrice(firstCard.link(), browser)).thenReturn(firstPrice);
        when(gatherer.getCardmarketCardPrice(secondCard.link(), browser)).thenReturn(secondPrice);

        try (MockedConstruction<PlaywrightBrowserContext> contexts = browserContext()) {
            saver.updateCardPrice(List.of(firstCard, secondCard));

            InOrder order = inOrder(gatherer, throttler, persister);
            order.verify(gatherer).getCardmarketCardPrice(firstCard.link(), browser);
            order.verify(throttler).waitBeforeNextRequest();
            order.verify(gatherer).getCardmarketCardPrice(secondCard.link(), browser);
            ArgumentCaptor<List<GatheredCardmarketPrice>> captor = ArgumentCaptor.forClass(List.class);
            order.verify(persister).saveAll(captor.capture());
            assertEquals(
                    List.of(
                            new GatheredCardmarketPrice("card-1", firstPrice),
                            new GatheredCardmarketPrice("card-2", secondPrice)),
                    captor.getValue());
            verify(contexts.constructed().getFirst()).close();
        }
    }

    @Test
    void doesNotPersistAnyPriceWhenGatheringFails() {
        CardmarketCardDto firstCard = card("card-1");
        CardmarketCardDto secondCard = card("card-2");
        when(gatherer.getCardmarketCardPrice(firstCard.link(), browser)).thenReturn(new CardmarketCardPrice());
        when(gatherer.getCardmarketCardPrice(secondCard.link(), browser))
                .thenThrow(new IllegalStateException("Rate limited"));

        try (@SuppressWarnings("unused")
                MockedConstruction<PlaywrightBrowserContext> ignored = browserContext()) {
            assertThrows(IllegalStateException.class, () -> saver.updateCardPrice(List.of(firstCard, secondCard)));
        }

        verify(persister, never()).saveAll(anyList());
    }

    @Test
    void doesNotThrottleBeforeTheFirstCard() {
        CardmarketCardDto card = card("card-1");
        when(gatherer.getCardmarketCardPrice(card.link(), browser)).thenReturn(new CardmarketCardPrice());

        try (@SuppressWarnings("unused")
                MockedConstruction<PlaywrightBrowserContext> ignored = browserContext()) {
            assertDoesNotThrow(() -> saver.updateCardPrice(List.of(card)));
        }

        verify(throttler, never()).waitBeforeNextRequest();
        verify(gatherer, times(1)).getCardmarketCardPrice(card.link(), browser);
    }

    private MockedConstruction<PlaywrightBrowserContext> browserContext() {
        return mockConstruction(PlaywrightBrowserContext.class, (mock, ignored) -> when(mock.getBrowser())
                .thenReturn(browser));
    }

    private static CardmarketCardDto card(String id) {
        return new CardmarketCardDto(id, "https://example.test/" + id, null, null, null, null, null);
    }
}
