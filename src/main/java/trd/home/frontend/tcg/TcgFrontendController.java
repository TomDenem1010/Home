package trd.home.frontend.tcg;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trd.home.tcg.service.TcgService;

@Controller
@RequestMapping("/tcg")
public class TcgFrontendController {

    private final TcgService tcgService;

    public TcgFrontendController(TcgService tcgService) {
        this.tcgService = tcgService;
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

    @PostMapping("/refresh-deck-prices")
    public String refreshDeckPrices() {
        tcgService.refreshDeckPrices();
        return "redirect:/tcg";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("deckPriceSummaries", tcgService.getDeckPriceSummary());
        return renderPage(model, "/tcg/statistics", "Statistics", "Az aktív deckek aktuális összértéke.");
    }

    @GetMapping("/statistic/{deckId}")
    public String deckPriceHistory(@PathVariable String deckId, Model model) {
        model.addAttribute("deckPriceHistorySummary", tcgService.getDeckPriceHistorySummary(deckId));
        return renderPage(model, "/tcg/statistics", "Deck price history", "A deck első és legutolsó ismert árai.");
    }

    private String renderPage(Model model, String activePath, String title, String content) {
        model.addAttribute("activePath", activePath);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageContent", content);
        return "index";
    }
}
