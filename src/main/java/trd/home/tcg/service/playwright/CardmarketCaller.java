package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import trd.home.tcg.exception.CardmarketRateLimitException;
import trd.home.tcg.exception.FailedToLaunchBrowser;
import trd.home.tcg.exception.HtmlParseException;

@Service
@NoArgsConstructor
public class CardmarketCaller {

    public Document callWithPlaywright(String url, Browser browser) {
        try {
            BrowserContext context = browser.contexts().get(0);
            Page page = context.pages().isEmpty()
                    ? context.newPage()
                    : context.pages().get(0);

            Response response = page.navigate(url);
            page.waitForLoadState();

            Document document = parseHtml(page.content());
            if ((response != null && response.status() == 429) || isRateLimitPage(document)) {
                throw new CardmarketRateLimitException("Cardmarket rate limit reached");
            }
            return document;
        } catch (HtmlParseException | CardmarketRateLimitException exception) {
            throw exception;
        } catch (Exception e) {
            throw new FailedToLaunchBrowser("Failed to launch browser", e);
        }
    }

    private boolean isRateLimitPage(Document document) {
        String text = document.text();
        return text.contains("You are being rate limited") || text.contains("Error 1015");
    }

    private Document parseHtml(String html) {
        try {
            return Jsoup.parse(html);
        } catch (Exception e) {
            throw new HtmlParseException("Failed to parse HTML", e);
        }
    }
}
