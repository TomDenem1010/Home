package trd.home.frontend.tcg;

import java.util.List;
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
import trd.home.tcg.constant.CardFoilType;
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

    @GetMapping("/deck-operations")
    public String deckOperations(Model model) {
        model.addAttribute("contentTemplate", "tcg/deck-operations");
        return renderPage(model, "/tcg/deck-operations", "Deck operations", "Manage saved decks.");
    }

    @PostMapping("/deck-operations/export-decks-to-csv")
    public String exportActiveDecksToCsv() {
        tcgService.exportActiveDecksToCsv();
        return "redirect:/tcg/deck-operations";
    }

    @PostMapping("/deck-operations/export-decks-to-xlsx")
    public String exportActiveDecks() {
        tcgService.exportActiveDecks();
        return "redirect:/tcg/deck-operations";
    }

    @PostMapping("/deck-operations/import-decks-from-csv")
    public String importDecksFromCsv() {
        tcgService.importDecksFromCsv();
        return "redirect:/tcg/deck-operations";
    }

    @PostMapping("/deck-operations/import-decks-from-xlsx")
    public String importDecksFromXlsx() {
        tcgService.importDecksFromXlsx();
        return "redirect:/tcg/deck-operations";
    }

    @PostMapping("/deck-operations/clear")
    public String clearDecks() {
        tcgService.clearDecks();
        return "redirect:/tcg/deck-operations";
    }

    @PostMapping("/decks/{deckId}/import-csv")
    public String importDeckFromCsv(@PathVariable String deckId) {
        tcgService.importDeckFromCsv(deckId);
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

    @GetMapping("/edit-decks")
    public String editDecks(Model model) {
        model.addAttribute("decks", tcgService.getVersionDecks());
        model.addAttribute("contentTemplate", "tcg/edit-decks");
        return renderPage(model, "/tcg/edit-decks", "Edit decks", "Select a deck to edit.");
    }

    @GetMapping("/edit-decks/{deckId}")
    public String editDeck(@PathVariable String deckId, Model model) {
        var deck = tcgService.getEditableDeck(deckId);
        model.addAttribute("deck", deck);
        model.addAttribute("foilTypes", tcgService.getCardFoilTypes());
        model.addAttribute("contentTemplate", "tcg/edit-deck");
        return renderPage(model, "/tcg/edit-decks", "Edit: " + deck.name(), "Each change creates a new version.");
    }

    @PostMapping("/edit-decks/{deckId}/remove")
    public String removeDeckCards(
            @PathVariable String deckId,
            @RequestParam String versionId,
            @RequestParam(required = false) List<String> cardIds) {
        tcgService.removeDeckCards(deckId, versionId, cardIds);
        return "redirect:/tcg/edit-decks/" + deckId;
    }

    @PostMapping("/edit-decks/{deckId}/add")
    public String addDeckCard(
            @PathVariable String deckId,
            @RequestParam String versionId,
            @RequestParam int quantity,
            @RequestParam String link,
            @RequestParam CardFoilType foilType) {
        tcgService.addDeckCard(deckId, versionId, quantity, link, foilType);
        return "redirect:/tcg/edit-decks/" + deckId;
    }

    private String renderPage(Model model, String activePath, String title, String content) {
        return pageRenderer.render(model, activePath, title, content, null, "tcg");
    }
}
