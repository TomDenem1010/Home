package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.dto.CardmarketCardDto;

@Service
@AllArgsConstructor
public class CardmarketCardPriceSaver {

    private final CardmarketCardPriceGatherer cardmarketCardPriceGatherer;
    private final CardmarketRequestThrottler requestThrottler;
    private final CardmarketCardPricePersister pricePersister;

    @LogMethodCall
    public void updateCardPrice(List<CardmarketCardDto> cardmarketCardDtos) {
        List<GatheredCardmarketPrice> gatheredPrices = new ArrayList<>();
        try (PlaywrightBrowserContext playwrightBrowserContext = new PlaywrightBrowserContext()) {
            Browser browser = playwrightBrowserContext.getBrowser();
            for (int index = 0; index < cardmarketCardDtos.size(); index++) {
                if (index > 0) {
                    requestThrottler.waitBeforeNextRequest();
                }
                CardmarketCardDto cardDto = cardmarketCardDtos.get(index);
                gatheredPrices.add(new GatheredCardmarketPrice(
                        cardDto.id(), cardmarketCardPriceGatherer.getCardmarketCardPrice(cardDto.link(), browser)));
            }
        }

        pricePersister.saveAll(gatheredPrices);
    }
}
