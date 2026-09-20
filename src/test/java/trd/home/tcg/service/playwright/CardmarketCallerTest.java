package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import org.junit.jupiter.api.Test;
import trd.home.common.exception.BrowserNavigationException;
import trd.home.common.playwright.BrowserPage;
import trd.home.common.playwright.PlaywrightPageReader;
import trd.home.tcg.exception.CardmarketRateLimitException;
import trd.home.tcg.exception.FailedToLaunchBrowser;
import trd.home.tcg.exception.HtmlParseException;

class CardmarketCallerTest {

    private final PlaywrightPageReader pageReader = mock(PlaywrightPageReader.class);
    private final CardmarketCaller caller = new CardmarketCaller(pageReader);
    private final Browser browser = mock(Browser.class);

    @Test
    void parsesBrowserPageContent() {
        givenPage(null, "<html><body><p>Card price</p></body></html>");

        assertEquals(
                "Card price",
                caller.callWithPlaywright("https://example.test/card", browser).text());
    }

    @Test
    void wrapsBrowserFailures() {
        when(pageReader.read("https://example.test/card", browser))
                .thenThrow(new BrowserNavigationException("Browser unavailable", null));

        assertThrows(
                FailedToLaunchBrowser.class, () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    @Test
    void propagatesHtmlParseFailures() {
        givenPage(null, null);

        assertThrows(HtmlParseException.class, () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    @Test
    void rejectsHttpRateLimitResponse() {
        givenPage(429, "<html />");

        assertThrows(
                CardmarketRateLimitException.class,
                () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    @Test
    void rejectsCloudflareRateLimitPage() {
        givenPage(null, "<h1>You are being rate limited</h1><p>Error 1015</p>");

        assertThrows(
                CardmarketRateLimitException.class,
                () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    private void givenPage(Integer statusCode, String content) {
        when(pageReader.read("https://example.test/card", browser)).thenReturn(new BrowserPage(statusCode, content));
    }
}
