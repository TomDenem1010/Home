package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trd.home.common.playwright.PlaywrightBrowserSessionFactory;
import trd.home.tcg.dto.CardmarketCardDto;

@Service
public class CardmarketCardPriceSaver {

    private final CardmarketCardPriceGatherer cardmarketCardPriceGatherer;
    private final CardmarketRequestThrottler requestThrottler;
    private final CardmarketCardPricePersister pricePersister;
    private final PlaywrightBrowserSessionFactory browserSessionFactory;
    private final String browserEndpoint;

    public CardmarketCardPriceSaver(
            CardmarketCardPriceGatherer cardmarketCardPriceGatherer,
            CardmarketRequestThrottler requestThrottler,
            CardmarketCardPricePersister pricePersister,
            PlaywrightBrowserSessionFactory browserSessionFactory,
            @Value("${tcg.cardmarket.browser-endpoint:http://localhost:9222}") String browserEndpoint) {
        this.cardmarketCardPriceGatherer = cardmarketCardPriceGatherer;
        this.requestThrottler = requestThrottler;
        this.pricePersister = pricePersister;
        this.browserSessionFactory = browserSessionFactory;
        this.browserEndpoint = browserEndpoint;
    }

    public void updateCardPrice(List<CardmarketCardDto> cardmarketCardDtos) {
        try (var browserSession = browserSessionFactory.open(browserEndpoint)) {
            Browser browser = browserSession.getBrowser();
            for (int index = 0; index < cardmarketCardDtos.size(); index++) {
                if (index > 0) {
                    requestThrottler.waitBeforeNextRequest();
                }
                CardmarketCardDto cardDto = cardmarketCardDtos.get(index);
                pricePersister.saveAll(List.of(new GatheredCardmarketPrice(
                        cardDto.id(), cardmarketCardPriceGatherer.getCardmarketCardPrice(cardDto.link(), browser))));
            }
        }
    }
}
