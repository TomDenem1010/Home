package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dto.CardmarketCardDto;
import trd.home.tcg.repository.CardmarketCardPriceRepository;
import trd.home.tcg.repository.CardmarketCardRepository;

@Service
@AllArgsConstructor
public class CardmarketCardPriceSaver {

    private final CardmarketCardPriceGatherer cardmarketCardPriceGatherer;
    private final CardmarketCardRepository cardmarketCardRepository;
    private final CardmarketCardPriceRepository cardmarketCardPriceRepository;

    @Transactional
    public void updateCardPrice(List<CardmarketCardDto> cardmarketCardDtos) {
        try (PlaywrightBrowserContext playwrightBrowserContext = new PlaywrightBrowserContext()) {
            cardmarketCardDtos.stream()
                    .map(cardDto -> getCardPrice(cardDto, playwrightBrowserContext.getBrowser()))
                    .forEach(cardPrice -> cardmarketCardPriceRepository.save(cardPrice));
        }
    }

    private CardmarketCardPrice getCardPrice(CardmarketCardDto cardDto, Browser browser) {
        CardmarketCardPrice cardPrice = cardmarketCardPriceGatherer.getCardmarketCardPrice(cardDto.link(), browser);
        cardPrice.setCard(cardmarketCardRepository.getReferenceById(cardDto.id()));
        return cardPrice;
    }
}
