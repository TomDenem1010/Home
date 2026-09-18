package trd.home.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class FrontendPageRendererTest {

    private final FrontendPageRenderer renderer = new FrontendPageRenderer();

    @Test
    void rendersBasicPageMetadata() {
        var model = new ConcurrentModel();

        assertEquals("index", renderer.render(model, "/path", "Title", "Description"));
        assertEquals("/path", model.getAttribute("activePath"));
        assertEquals("Title", model.getAttribute("pageTitle"));
        assertEquals("Description", model.getAttribute("pageContent"));
        assertNull(model.getAttribute("contentTemplate"));
        assertNull(model.getAttribute("featureStylesheet"));
        assertNull(model.getAttribute("featureScript"));
    }

    @Test
    void rendersFeaturePageMetadata() {
        var model = new ConcurrentModel();

        renderer.render(model, "/path", "Title", "Description", "feature/content", "feature");

        assertEquals("feature/content", model.getAttribute("contentTemplate"));
        assertEquals("/css/feature.css", model.getAttribute("featureStylesheet"));
        assertEquals("/js/feature.js", model.getAttribute("featureScript"));
    }
}
