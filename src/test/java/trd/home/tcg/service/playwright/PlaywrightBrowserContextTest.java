package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PlaywrightBrowserContextTest {

    @Test
    void connectsToChromeOverCdpAndClosesItsResources() {
        Playwright playwright = Mockito.mock(Playwright.class);
        BrowserType chromium = Mockito.mock(BrowserType.class);
        Browser browser = Mockito.mock(Browser.class);
        when(playwright.chromium()).thenReturn(chromium);
        when(chromium.connectOverCDP("http://localhost:9222")).thenReturn(browser);

        try (MockedStatic<Playwright> playwrightFactory = mockStatic(Playwright.class)) {
            playwrightFactory.when(Playwright::create).thenReturn(playwright);

            PlaywrightBrowserContext context = new PlaywrightBrowserContext();

            assertSame(browser, context.getBrowser());
            context.close();
        }

        verify(browser).close();
        verify(playwright).close();
    }
}
