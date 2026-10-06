package trd.home.frontend.tcg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckVersionHistory;
import trd.home.tcg.dto.DeckVersionListItem;
import trd.home.tcg.dto.DeckVersionSummary;
import trd.home.tcg.service.TcgService;

class DeckVersionPageTest {
    @Test
    void rendersDeckLinksAndBothChangeColumnsAndEmptyStates() throws Exception {
        var service = mock(TcgService.class);
        when(service.getVersionDecks()).thenReturn(List.of(new DeckVersionListItem("deck", "Test deck")));
        var change = new DeckVersionSummary.CardChange(
                "Test Card", "https://www.cardmarket.com/en/Magic/Products/Singles/Test-Card", CardFoilType.FOIL, 2);
        when(service.getDeckVersionHistory("deck"))
                .thenReturn(new DeckVersionHistory(
                        "Test deck",
                        List.of(
                                new DeckVersionSummary("v1", List.of(change), List.of()),
                                new DeckVersionSummary("v2", List.of(), List.of(change)))));
        when(service.getDeckVersionHistory("empty")).thenReturn(new DeckVersionHistory("Empty", List.of()));
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        var mvc = MockMvcBuilders.standaloneSetup(new TcgFrontendController(service, new FrontendPageRenderer()))
                .setViewResolvers(views)
                .build();
        var listing = Jsoup.parse(mvc.perform(get("/tcg/version"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8));
        assertEquals(
                "/tcg/version/deck", listing.selectFirst(".tcg-page tbody a").attr("href"));
        var html = Jsoup.parse(mvc.perform(get("/tcg/version/deck"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8));
        assertEquals(
                List.of("v1", "v2"),
                html.select("details.version-entry > summary").eachText());
        assertTrue(html.select("details.version-entry[open]").isEmpty());
        assertEquals(
                List.of("+", "−"),
                html.selectFirst(".version-changes").select("th").eachText());
        assertTrue(html.select(".version-changes td").get(0).text().contains("2 × Test Card (FOIL)"));
        assertTrue(html.select(".version-changes td").get(1).text().contains("No removals."));
        assertEquals(change.link(), html.selectFirst(".version-card-list a").attr("href"));
        assertTrue(html.select(".version-changes td").get(3).text().contains("2 × Test Card (FOIL)"));
        var empty = mvc.perform(get("/tcg/version/empty"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertTrue(empty.contains("There are no saved versions"));
        when(service.getVersionDecks()).thenReturn(List.of());
        assertTrue(mvc.perform(get("/tcg/version"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("There are no active decks"));
    }
}
