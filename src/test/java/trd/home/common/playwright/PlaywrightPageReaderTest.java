package trd.home.common.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trd.home.common.exception.BrowserNavigationException;

@ExtendWith(MockitoExtension.class)
class PlaywrightPageReaderTest {

    private final PlaywrightPageReader reader = new PlaywrightPageReader();

    @Mock
    private Browser browser;

    @Mock
    private BrowserContext context;

    @Mock
    private Page page;

    @Mock
    private Response response;

    @BeforeEach
    void setUp() {
        when(browser.contexts()).thenReturn(List.of(context));
    }

    @Test
    void opensPageAndReturnsStatusAndContent() {
        when(context.pages()).thenReturn(List.of());
        when(context.newPage()).thenReturn(page);
        when(page.navigate("https://example.test")).thenReturn(response);
        when(response.status()).thenReturn(200);
        when(page.content()).thenReturn("<html />");

        BrowserPage result = reader.read("https://example.test", browser);

        assertEquals(200, result.statusCode());
        assertEquals("<html />", result.content());
        verify(context).newPage();
        verify(page).waitForLoadState();
    }

    @Test
    void reusesExistingPageAndAllowsMissingResponse() {
        when(context.pages()).thenReturn(List.of(page));
        when(page.content()).thenReturn("content");

        BrowserPage result = reader.read("https://example.test", browser);

        assertNull(result.statusCode());
        assertEquals("content", result.content());
    }

    @Test
    void wrapsBrowserFailure() {
        when(browser.contexts()).thenThrow(new IllegalStateException("unavailable"));

        assertThrows(BrowserNavigationException.class, () -> reader.read("https://example.test", browser));
    }
}
