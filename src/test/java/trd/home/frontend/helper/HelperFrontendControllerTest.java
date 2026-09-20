package trd.home.frontend.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import trd.home.common.browser.ChromeBrowserLauncher;

class HelperFrontendControllerTest {

    private final ChromeBrowserLauncher launcher = mock(ChromeBrowserLauncher.class);
    private final HelperFrontendController controller = new HelperFrontendController(launcher);

    @Test
    void startsChromeAndRedirectsHome() {
        assertEquals("redirect:/", controller.startChrome());

        verify(launcher).start();
    }
}
