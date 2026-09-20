package trd.home.common.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import lombok.Getter;
import trd.home.common.exception.BrowserConnectionException;

@Getter
public class PlaywrightBrowserSession implements AutoCloseable {

    private final Playwright playwright;
    private final Browser browser;

    PlaywrightBrowserSession(String endpoint) {
        Playwright createdPlaywright = Playwright.create();
        try {
            browser = createdPlaywright.chromium().connectOverCDP(endpoint);
            playwright = createdPlaywright;
        } catch (RuntimeException exception) {
            closeAfterFailedConnection(createdPlaywright, exception);
            throw new BrowserConnectionException("Unable to connect to browser: " + endpoint, exception);
        }
    }

    @Override
    public void close() {
        try {
            browser.close();
        } finally {
            playwright.close();
        }
    }

    private static void closeAfterFailedConnection(Playwright playwright, RuntimeException connectionFailure) {
        try {
            playwright.close();
        } catch (RuntimeException closeFailure) {
            connectionFailure.addSuppressed(closeFailure);
        }
    }
}
