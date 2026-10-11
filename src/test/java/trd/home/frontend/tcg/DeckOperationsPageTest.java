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
    void rendersPostButtonsAndQueuesOperations() throws Exception {
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
        assertEquals(4, html.select(".deck-operations form[method=post]").size());
        assertEquals(4, html.select(".deck-operations button").size());
        assertEquals(
                "Export XLSX",
                html.selectFirst("form[action='/tcg/deck-operations/export-decks-to-xlsx'][method=post] button")
                        .text());
        assertEquals(
                "Import CSV",
                html.selectFirst("form[action='/tcg/deck-operations/import-decks-from-csv'] button")
                        .text());
        assertEquals(
                "Clear",
                html.selectFirst("form[action='/tcg/deck-operations/clear'] button")
                        .text());
        assertEquals(
                "Import XLSX",
                html.selectFirst("form[action='/tcg/deck-operations/import-decks-from-xlsx'] button")
                        .text());
        assertTrue(html.select("input[type=file]").isEmpty());
        mvc.perform(post("/tcg/deck-operations/import-decks-from-xlsx"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tcg/deck-operations"));
        verify(service).importDecksFromXlsx();
        mvc.perform(post("/tcg/deck-operations/import-decks-from-csv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tcg/deck-operations"));
        verify(service).importDecksFromCsv();
        mvc.perform(get("/tcg/deck-operations/import-decks-from-csv")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/tcg/deck-operations/import-decks-from-xlsx")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/tcg/deck-operations/refresh")).andExpect(status().isNotFound());
        mvc.perform(post("/tcg/deck-operations/clear"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tcg/deck-operations"));
        verify(service).clearDecks();
        mvc.perform(get("/tcg/deck-operations/clear")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/tcg/deck-operations/export-decks-to-xlsx"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tcg/deck-operations"));
        verify(service).exportActiveDecks();
        mvc.perform(get("/tcg/deck-operations/export-decks-to-xlsx")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/tcg/deck-operations/export")).andExpect(status().isNotFound());
        verifyNoMoreInteractions(service);
    }
}
