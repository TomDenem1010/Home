package trd.home.frontend.tcg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trd.home.frontend.FrontendPageRenderer;
import trd.home.tcg.service.TcgService;

@Controller
@RequestMapping("/tcg")
@RequiredArgsConstructor
public class TcgFrontendController {

    private final TcgService tcgService;
    private final FrontendPageRenderer pageRenderer;

    @GetMapping
    public String tcg(Model model) {
        return renderPage(model, "/tcg", "TCG", "A TCG funkciók itt érhetők el.");
    }

    @PostMapping("/save-decks-from-resource")
    public String saveDecksFromResource() {
        tcgService.saveDecksFromResource();
        return "redirect:/tcg";
    }

    @PostMapping("/refresh-deck-prices")
    public String refreshDeckPrices() {
        tcgService.refreshDeckPrices();
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
