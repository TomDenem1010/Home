package trd.home.frontend.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import trd.home.common.browser.ChromeBrowserLauncher;
import trd.home.frontend.FrontendPageRenderer;

class HelperFrontendControllerTest {

    private final ChromeBrowserLauncher chromeBrowserLauncher = mock(ChromeBrowserLauncher.class);
    private final HelperFrontendController controller =
            new HelperFrontendController(chromeBrowserLauncher, new FrontendPageRenderer(), "");

    @Test
    void startsContainerChromeAndOpensDesktopView() {
        var dockerController = new HelperFrontendController(
                chromeBrowserLauncher, new FrontendPageRenderer(), "http://localhost:6080/vnc.html");
        assertEquals("redirect:/helper/chrome", dockerController.startChrome());
        verify(chromeBrowserLauncher).start();

        var model = new ConcurrentModel();
        assertEquals("index", dockerController.chrome(model));
        assertEquals("helper/chrome", model.getAttribute("contentTemplate"));
        assertEquals("http://localhost:6080/vnc.html", model.getAttribute("chromeDesktopUrl"));
    }

    @Test
    void startsChromeAndRedirectsHome() {
        assertEquals("redirect:/", controller.startChrome());

        verify(chromeBrowserLauncher).start();
    }
}
