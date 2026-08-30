package trd.home.tcg.service.playwright;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trd.home.tcg.exception.CardmarketRateLimitException;
import trd.home.tcg.exception.FailedToLaunchBrowser;
import trd.home.tcg.exception.HtmlParseException;

@ExtendWith(MockitoExtension.class)
class CardmarketCallerTest {

    private final CardmarketCaller caller = new CardmarketCaller();

    @Mock
    private Browser browser;

    @Mock
    private BrowserContext context;

    @Mock
    private Page page;

    @Mock
    private Response response;

    @Test
    void opensPageInFirstContextAndParsesItsHtml() {
        when(browser.contexts()).thenReturn(List.of(context));
        when(context.pages()).thenReturn(List.of());
        when(context.newPage()).thenReturn(page);
        when(page.content()).thenReturn("<html><body><p>Card price</p></body></html>");

        assertEquals(
                "Card price",
                caller.callWithPlaywright("https://example.test/card", browser).text());

        verify(context).newPage();
        verify(page).navigate("https://example.test/card");
        verify(page).waitForLoadState();
    }

    @Test
    void reusesFirstExistingPage() {
        when(browser.contexts()).thenReturn(List.of(context));
        when(context.pages()).thenReturn(List.of(page));
        when(page.content()).thenReturn("<html />");

        assertDoesNotThrow(() -> caller.callWithPlaywright("https://example.test/card", browser));

        verify(page).navigate("https://example.test/card");
        verify(page).waitForLoadState();
    }

    @Test
    void wrapsBrowserFailures() {
        when(browser.contexts()).thenThrow(new IllegalStateException("Browser unavailable"));

        assertThrows(
                FailedToLaunchBrowser.class, () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    @Test
    void propagatesHtmlParseFailures() {
        when(browser.contexts()).thenReturn(List.of(context));
        when(context.pages()).thenReturn(List.of(page));
        when(page.content()).thenReturn(null);

        assertThrows(HtmlParseException.class, () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    @Test
    void rejectsHttpRateLimitResponse() {
        when(browser.contexts()).thenReturn(List.of(context));
        when(context.pages()).thenReturn(List.of(page));
        when(page.navigate("https://example.test/card")).thenReturn(response);
        when(response.status()).thenReturn(429);
        when(page.content()).thenReturn("<html />");

        assertThrows(
                CardmarketRateLimitException.class,
                () -> caller.callWithPlaywright("https://example.test/card", browser));
    }

    @Test
    void rejectsCloudflareRateLimitPage() {
        when(browser.contexts()).thenReturn(List.of(context));
        when(context.pages()).thenReturn(List.of(page));
        when(page.content()).thenReturn("<h1>You are being rate limited</h1><p>Error 1015</p>");

        assertThrows(
                CardmarketRateLimitException.class,
                () -> caller.callWithPlaywright("https://example.test/card", browser));
    }
}
