package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import trd.home.tcg.dao.CardmarketCardPrice;

@Slf4j
@Service
@AllArgsConstructor
public class CardmarketCardPriceGatherer {

    private final CardmarketCaller cardmarketCaller;

    public CardmarketCardPrice getCardmarketCardPrice(String link, Browser browser) {
        Document document = cardmarketCaller.callWithPlaywright(link, browser);

        CardmarketCardPrice cardPrice = new CardmarketCardPrice();
        cardPrice.setFromInEuro(getValue(document, "From"));
        cardPrice.setTrendInEuro(getValue(document, "Price Trend"));

        return cardPrice;
    }

    private BigDecimal getValue(Document document, String label) {
        Element dt = document.select("dt").stream()
                .filter(e -> label.equals(e.text().trim()))
                .findFirst()
                .orElse(null);

        if (Objects.isNull(dt)) {
            log.debug("Missing Cardmarket price label: {}", label);
            return BigDecimal.ZERO;
        }

        Element value = dt.nextElementSibling();
        if (Objects.isNull(value)) {
            log.debug("Missing Cardmarket price value for label: {}", label);
            return BigDecimal.ZERO;
        }

        return new BigDecimal(parseEuroToDoubleString(value.text().trim()));
    }

    private String parseEuroToDoubleString(String value) {
        return value.replace("€", "").replace(".", "").replace(",", ".").trim();
    }
}
