package trd.home.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class FrontendControllerTest {

    private final FrontendController controller = new FrontendController();

    @Test
    void homeReturnsIndexView() {
        var model = new ConcurrentModel();

        var view = controller.home(model);

        assertEquals("index", view);
        assertEquals("/", model.getAttribute("activePath"));
    }
}
