package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.exception.CardmarketRateLimitException;
import trd.home.tcg.exception.FailedToLaunchBrowser;
import trd.home.tcg.exception.HtmlParseException;

@Slf4j
@Service
@NoArgsConstructor
public class CardmarketCaller {

    @LogMethodCall
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
            log.error("Cardmarket request failed for URL '{}'", url, exception);
            throw exception;
        } catch (Exception exception) {
            log.error("Unexpected error while calling Cardmarket URL '{}'", url, exception);
            throw new FailedToLaunchBrowser("Failed to launch browser", exception);
        }
    }

    private boolean isRateLimitPage(Document document) {
        String text = document.text();
        return text.contains("You are being rate limited") || text.contains("Error 1015");
    }

    private Document parseHtml(String html) {
        try {
            return Jsoup.parse(html);
        } catch (Exception exception) {
            log.error("Failed to parse HTML returned by Cardmarket", exception);
            throw new HtmlParseException("Failed to parse HTML", exception);
        }
    }
}
