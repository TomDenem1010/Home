package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import trd.home.tcg.exception.FailedToLaunchBrowser;
import trd.home.tcg.exception.HtmlParseException;

@Slf4j
@Service
@NoArgsConstructor
public class CardmarketCaller {

    public Document callWithPlaywright(String url, Browser browser) {
        try {
            BrowserContext context = browser.contexts().get(0);
            Page page = context.pages().isEmpty()
                    ? context.newPage()
                    : context.pages().get(0);

            page.navigate(url);
            page.waitForLoadState();

            return parseHtml(page.content());
        } catch (HtmlParseException exception) {
            throw exception;
        } catch (Exception e) {
            log.error("Unexpected error occurred while calling URL: {}", url, e);
            throw new FailedToLaunchBrowser("Failed to launch browser", e);
        }
    }

    private Document parseHtml(String html) {
        try {
            Document document = Jsoup.parse(html);
            log.debug("Successfully parsed HTML document: {}", document.toString());
            return document;
        } catch (Exception e) {
            log.error("Failed to parse HTML", e);
            throw new HtmlParseException("Failed to parse HTML", e);
        }
    }
}
