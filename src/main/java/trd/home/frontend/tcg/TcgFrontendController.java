package trd.home.frontend.tcg;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trd.home.common.logging.LogMethodCall;
import trd.home.tcg.service.TcgService;

@Controller
@RequestMapping("/tcg")
public class TcgFrontendController {

    private final TcgService tcgService;

    public TcgFrontendController(TcgService tcgService) {
        this.tcgService = tcgService;
    }

    @GetMapping
    @LogMethodCall
    public String tcg(Model model) {
        return renderPage(model, "/tcg", "TCG", "A TCG funkciók itt érhetők el.");
    }

    @PostMapping("/save-decks-from-resource")
    @LogMethodCall
    public String saveDecksFromResource() {
        tcgService.saveDecksFromResource();
        return "redirect:/tcg";
    }

    @PostMapping("/refresh-deck-prices")
    @LogMethodCall
    public String refreshDeckPrices() {
        tcgService.refreshDeckPrices();
        return "redirect:/tcg";
    }

    @GetMapping("/statistics")
    @LogMethodCall
    public String statistics(Model model) {
        model.addAttribute("deckPriceSummaries", tcgService.getDeckPriceSummary());
        model.addAttribute("contentTemplate", "tcg/statistics");
        return renderPage(model, "/tcg/statistics", "Statistics", "Current total value of active decks.");
    }

    @GetMapping("/statistic/{deckId}")
    @LogMethodCall
    public String deckPriceHistory(@PathVariable String deckId, Model model) {
        model.addAttribute("deckPriceHistorySummary", tcgService.getDeckPriceHistorySummary(deckId));
        model.addAttribute("contentTemplate", "tcg/statistics-uuid");
        return renderPage(model, "/tcg/statistics", "Deck price history", "Latest known card prices for this deck.");
    }

    private String renderPage(Model model, String activePath, String title, String content) {
        model.addAttribute("activePath", activePath);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageContent", content);
        return "index";
    }
}
