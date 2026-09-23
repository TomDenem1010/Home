package trd.home.common.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import trd.home.common.exception.BrowserConnectionException;

class PlaywrightBrowserSessionTest {

    @Test
    void connectsToEndpointAndClosesResources() {
        Playwright playwright = Mockito.mock(Playwright.class);
        BrowserType chromium = Mockito.mock(BrowserType.class);
        Browser browser = Mockito.mock(Browser.class);
        when(playwright.chromium()).thenReturn(chromium);
        when(chromium.connectOverCDP("http://localhost:9222")).thenReturn(browser);

        try (MockedStatic<Playwright> factory = mockStatic(Playwright.class)) {
            factory.when(Playwright::create).thenReturn(playwright);

            PlaywrightBrowserSession session = new PlaywrightBrowserSession("http://localhost:9222");

            assertSame(browser, session.getBrowser());
            assertSame(playwright, session.getPlaywright());
            session.close();
        }

        verify(browser).close();
        verify(playwright).close();
    }

    @Test
    void closesPlaywrightWhenBrowserConnectionFails() {
        Playwright playwright = Mockito.mock(Playwright.class);
        BrowserType chromium = Mockito.mock(BrowserType.class);
        when(playwright.chromium()).thenReturn(chromium);
        when(chromium.connectOverCDP("invalid-endpoint")).thenThrow(new IllegalStateException("unavailable"));

        try (MockedStatic<Playwright> factory = mockStatic(Playwright.class)) {
            factory.when(Playwright::create).thenReturn(playwright);

            assertThrows(BrowserConnectionException.class, () -> new PlaywrightBrowserSession("invalid-endpoint"));
        }

        verify(playwright).close();
    }

    @Test
    void suppressesCloseFailureOnConnectionFailure() {
        Playwright playwright = Mockito.mock(Playwright.class);
        BrowserType chromium = Mockito.mock(BrowserType.class);
        IllegalStateException connectionFailure = new IllegalStateException("unavailable");
        IllegalStateException closeFailure = new IllegalStateException("close failed");
        when(playwright.chromium()).thenReturn(chromium);
        when(chromium.connectOverCDP("invalid-endpoint")).thenThrow(connectionFailure);
        org.mockito.Mockito.doThrow(closeFailure).when(playwright).close();

        try (MockedStatic<Playwright> factory = mockStatic(Playwright.class)) {
            factory.when(Playwright::create).thenReturn(playwright);

            BrowserConnectionException exception = assertThrows(
                    BrowserConnectionException.class, () -> new PlaywrightBrowserSession("invalid-endpoint"));

            assertSame(connectionFailure, exception.getCause());
            assertEquals(1, connectionFailure.getSuppressed().length);
            assertSame(closeFailure, connectionFailure.getSuppressed()[0]);
        }
    }
}
