package trd.home.common.playwright;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PlaywrightBrowserSessionFactoryTest {

    @Test
    void opensConnectedSession() {
        Playwright playwright = Mockito.mock(Playwright.class);
        BrowserType chromium = Mockito.mock(BrowserType.class);
        Browser browser = Mockito.mock(Browser.class);
        when(playwright.chromium()).thenReturn(chromium);
        when(chromium.connectOverCDP("http://localhost:9222")).thenReturn(browser);

        try (MockedStatic<Playwright> staticFactory = mockStatic(Playwright.class)) {
            staticFactory.when(Playwright::create).thenReturn(playwright);

            PlaywrightBrowserSession session = new PlaywrightBrowserSessionFactory().open("http://localhost:9222");

            assertSame(playwright, session.getPlaywright());
            assertSame(browser, session.getBrowser());
        }
    }
}
