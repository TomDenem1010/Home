package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.exception.CardmarketPriceNotFoundException;

@Service
@AllArgsConstructor
public class CardmarketCardPriceGatherer {

    private final CardmarketCaller cardmarketCaller;

    public CardmarketCardPrice getCardmarketCardPrice(String link, Browser browser) {
        Document document = cardmarketCaller.callWithPlaywright(link, browser);

        CardmarketCardPrice cardPrice = new CardmarketCardPrice();
        cardPrice.setFromInEuro(getValue(document, "From", link));
        cardPrice.setTrendInEuro(getValue(document, "Price Trend", link));

        return cardPrice;
    }

    private BigDecimal getValue(Document document, String label, String link) {
        Element dt = document.select("dt").stream()
                .filter(element -> label.equals(element.text().trim()))
                .findFirst()
                .orElseThrow(() -> priceNotFound(label, link));
        Element value = dt.nextElementSibling();
        if (value == null || value.text().isBlank()) {
            throw priceNotFound(label, link);
        }

        try {
            BigDecimal price =
                    new BigDecimal(parseEuroToDecimalString(value.text().trim()));
            if (price.signum() <= 0) {
                throw priceNotFound(label, link);
            }
            return price;
        } catch (NumberFormatException exception) {
            throw new CardmarketPriceNotFoundException(
                    "Unable to parse Cardmarket '" + label + "' price for: " + link, exception);
        }
    }

    private String parseEuroToDecimalString(String value) {
        return value.replace("€", "").replace(".", "").replace(",", ".").trim();
    }

    private CardmarketPriceNotFoundException priceNotFound(String label, String link) {
        return new CardmarketPriceNotFoundException("Cardmarket '" + label + "' price was not found for: " + link);
    }
}
