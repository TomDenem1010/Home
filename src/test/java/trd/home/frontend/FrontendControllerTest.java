package trd.home.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class FrontendControllerTest {

    private final FrontendController controller = new FrontendController();

    @Test
    void homeReturnsMenuItemsAndIndexView() {
        var model = new ConcurrentModel();

        var view = controller.home(model);

        assertEquals("index", view);
        assertEquals(
                List.of(new MenuItem("Kezdőlap", "/"), new MenuItem("TCG", "/tcg")), model.getAttribute("menuItems"));
        assertEquals("/", model.getAttribute("activePath"));
    }

    @Test
    void tcgReturnsTheSameMenuAndMarksTcgAsActive() {
        var model = new ConcurrentModel();

        var view = controller.tcg(model);

        assertEquals("index", view);
        assertEquals(2, ((List<?>) model.getAttribute("menuItems")).size());
        assertEquals("/tcg", model.getAttribute("activePath"));
    }
}
