package trd.home.frontend.tcg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.CardSearchDeck;
import trd.home.tcg.dto.CardSearchResult;
import trd.home.tcg.service.TcgService;

class CardSearchPageTest {
    @Test
    void rendersBothUnitPricesDeckQuantitiesAndServerPagination() throws Exception {
        var service = mock(TcgService.class);
        when(service.getCardFoilTypes()).thenReturn(CardFoilType.values());
        when(service.getCardGameTypes()).thenReturn(trd.home.tcg.constant.CardGameType.values());
        when(service.getCardPriceTypes()).thenReturn(trd.home.tcg.constant.CardPriceType.values());
        var card = new CardSearchResult(
                "card",
                "Test Card",
                "https://www.cardmarket.com/en/Magic/Products/Singles/Test-Card?language=1&isFoil=Y",
                CardFoilType.FOIL,
                7,
                new BigDecimal("2.50"),
                new BigDecimal("3.75"),
                List.of(new CardSearchDeck("deck-a", "Deck A", 3), new CardSearchDeck("deck-b", "Deck B", 4)));
        when(service.searchCards(
                        any(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new PageImpl<>(List.of(card), PageRequest.of(1, 10), 25));
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
        var response = mvc.perform(get("/tcg/search-card").param("size", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var html = Jsoup.parse(response.getContentAsString());
        assertEquals(
                card.link(), html.selectFirst(".card-search-results tbody a").attr("href"));
        assertEquals(
                "Test Card", html.selectFirst(".card-search-results tbody a").text());
        assertEquals(
                6,
                html.select(".card-search-results th button[data-server-sort]").size());
        assertEquals(2, html.select(".card-search-decks li").size());
        assertTrue(html.selectFirst(".card-search-decks").text().contains("Deck A (3)"));
        assertTrue(html.selectFirst(".card-search-decks").text().contains("Deck B (4)"));
        assertTrue(html.selectFirst(".card-search-pagination").text().contains("Page 2 / 3 (25 cards)"));
        assertEquals(2, html.select("input[name^=price]").size());
        assertEquals(2, html.select("select[name=priceType] option").size());
        assertEquals(1, html.select(".card-search-toolbar select[name=size]").size());
        assertTrue(html.select("input[name=size]").isEmpty());
    }
}
