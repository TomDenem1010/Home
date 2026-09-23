package trd.home.common.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.springframework.stereotype.Component;
import trd.home.common.exception.BrowserNavigationException;

@Component
public class PlaywrightPageReader {

    public BrowserPage read(String url, Browser browser) {
        try {
            BrowserContext context = browser.contexts().getFirst();
            Page page = context.pages().isEmpty()
                    ? context.newPage()
                    : context.pages().getFirst();
            Response response = page.navigate(url);
            page.waitForLoadState();
            return new BrowserPage(response == null ? 0 : response.status(), page.content());
        } catch (RuntimeException exception) {
            throw new BrowserNavigationException("Unable to read browser page: " + url, exception);
        }
    }
}
