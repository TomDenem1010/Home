package trd.home.frontend.tcg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.tcg.service.TcgService;

class DeckOperationsPageTest {
    @Test
    void rendersPostButtonsAndQueuesBothOperations() throws Exception {
        var service = mock(TcgService.class);
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        var mvc = MockMvcBuilders.standaloneSetup(new TcgFrontendController(service, new FrontendPageRenderer()))
                .setViewResolvers(views)
                .build();
        var html = Jsoup.parse(mvc.perform(get("/tcg/deck-operations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        assertEquals(2, html.select(".deck-operations form[method=post]").size());
        assertEquals(3, html.select(".deck-operations button").size());
        assertEquals(
                "Download Excel",
                html.selectFirst("form[action='/tcg/deck-operations/export'][method=get] button")
                        .text());
        assertEquals(
                "Refresh",
                html.selectFirst("form[action='/tcg/deck-operations/refresh'] button")
                        .text());
        assertEquals(
                "Clear",
                html.selectFirst("form[action='/tcg/deck-operations/clear'] button")
                        .text());
        mvc.perform(post("/tcg/deck-operations/refresh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tcg/deck-operations"));
        verify(service).saveDecksFromResource();
        mvc.perform(post("/tcg/deck-operations/clear"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tcg/deck-operations"));
        verify(service).clearDecks();
        mvc.perform(get("/tcg/deck-operations/clear")).andExpect(status().isMethodNotAllowed());
        byte[] workbook = {80, 75, 3, 4};
        when(service.exportActiveDecks()).thenReturn(workbook);
        mvc.perform(get("/tcg/deck-operations/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                                "Content-Disposition",
                                org.hamcrest.Matchers.matchesPattern(
                                        "attachment; filename=\"active-decks_[0-9]{14}\\.xlsx\"")))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(workbook));
        verify(service).exportActiveDecks();
        verifyNoMoreInteractions(service);
    }
}
