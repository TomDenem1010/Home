package trd.home.frontend.tcg;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private static final DateTimeFormatter EXPORT_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT).withZone(ZoneOffset.UTC);

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

    @GetMapping("/deck-operations")
    public String deckOperations(Model model) {
        model.addAttribute("contentTemplate", "tcg/deck-operations");
        return renderPage(model, "/tcg/deck-operations", "Deck operations", "Manage saved decks.");
    }

    @GetMapping("/deck-operations/export")
    public ResponseEntity<byte[]> exportActiveDecks() {
        String filename = "active-decks_" + EXPORT_TIMESTAMP.format(Instant.now()) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(tcgService.exportActiveDecks());
    }

    @PostMapping({"/save-decks-from-resource", "/deck-operations/refresh"})
    public String saveDecksFromResource() {
        tcgService.saveDecksFromResource();
        return "redirect:/tcg/deck-operations";
    }

    @PostMapping("/deck-operations/clear")
    public String clearDecks() {
        tcgService.clearDecks();
        return "redirect:/tcg/deck-operations";
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

    @GetMapping("/version")
    public String versions(Model model) {
        model.addAttribute("decks", tcgService.getVersionDecks());
        model.addAttribute("contentTemplate", "tcg/versions");
        return renderPage(model, "/tcg/version", "Versions", "Version history of active decks.");
    }

    @GetMapping("/version/{deckId}")
    public String deckVersions(@PathVariable String deckId, Model model) {
        var history = tcgService.getDeckVersionHistory(deckId);
        model.addAttribute("history", history);
        model.addAttribute("contentTemplate", "tcg/version-history");
        return renderPage(
                model,
                "/tcg/version",
                "Versions: " + history.deckName(),
                "Card changes compared with the previous saved version.");
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
