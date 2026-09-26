package trd.home.frontend.tcg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.tcg.dto.CardSearchFilter;
import trd.home.tcg.service.TcgService;

@Controller
@RequestMapping("/tcg")
@RequiredArgsConstructor
public class TcgFrontendController {

    private final TcgService tcgService;
    private final FrontendPageRenderer pageRenderer;

    @GetMapping("/search-card")
    public String searchCard(
            @ModelAttribute("filter") CardSearchFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("foilTypes", tcgService.getCardFoilTypes());
        model.addAttribute("cardGameTypes", tcgService.getCardGameTypes());
        model.addAttribute("cardPriceTypes", tcgService.getCardPriceTypes());
        model.addAttribute("contentTemplate", "tcg/search-card");
        model.addAttribute("searchResults", tcgService.searchCards(filter, page, size, sort, direction));
        return renderPage(
                model,
                "/tcg/search-card",
                "SearchCard",
                "Search cards in active decks. Latest known prices in EUR per card.");
    }

    @GetMapping
    public String tcg(Model model) {
        return renderPage(model, "/tcg", "TCG", "A TCG funkciók itt érhetők el.");
    }

    @PostMapping("/save-decks-from-resource")
    public String saveDecksFromResource() {
        tcgService.saveDecksFromResource();
        return "redirect:/tcg";
    }

    @PostMapping("/decks/{deckId}/reload")
    public String reloadDeck(@PathVariable String deckId) {
        tcgService.saveDeckFromResource(deckId);
        return "redirect:/tcg/statistics";
    }

    @PostMapping("/decks/{deckId}/refresh-prices")
    public String refreshDeckPrices(@PathVariable String deckId) {
        tcgService.refreshDeckPrices(deckId);
        return "redirect:/tcg/statistics";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("deckPriceSummaries", tcgService.getDeckPriceSummary());
        model.addAttribute("contentTemplate", "tcg/statistics");
        return renderPage(model, "/tcg/statistics", "Statistics", "Current total value of active decks.");
    }

    @GetMapping("/statistic/{deckId}")
    public String deckPriceHistory(@PathVariable String deckId, Model model) {
        model.addAttribute("deckPriceHistorySummary", tcgService.getDeckPriceHistorySummary(deckId));
        model.addAttribute("contentTemplate", "tcg/statistics-uuid");
        return renderPage(model, "/tcg/statistics", "Deck price history", "Latest known card prices for this deck.");
    }

    private String renderPage(Model model, String activePath, String title, String content) {
        return pageRenderer.render(model, activePath, title, content, null, "tcg");
    }
}
