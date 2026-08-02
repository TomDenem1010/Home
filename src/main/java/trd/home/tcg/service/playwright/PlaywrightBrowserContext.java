package trd.home.tcg.service.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import java.util.Objects;
import lombok.Getter;

@Getter
public class PlaywrightBrowserContext implements AutoCloseable {

    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightBrowserContext() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().connectOverCDP("http://localhost:9222");
    }

    @Override
    public void close() {
        if (Objects.nonNull(browser)) {
            browser.close();
        }

        if (Objects.nonNull(playwright)) {
            playwright.close();
        }
    }
}
