package trd.home.frontend.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import trd.home.tcg.service.TcgService;

class HelperFrontendControllerTest {

    private final TcgService tcgService = mock(TcgService.class);
    private final HelperFrontendController controller = new HelperFrontendController(tcgService);

    @Test
    void startsChromeAndRedirectsHome() {
        assertEquals("redirect:/", controller.startChrome());

        verify(tcgService).startChrome();
    }
}
