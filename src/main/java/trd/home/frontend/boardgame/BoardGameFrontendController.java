package trd.home.frontend.boardgame;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.constant.BoardGameType;
import trd.home.boardgame.dto.*;
import trd.home.boardgame.exception.BoardGameNotFoundException;
import trd.home.boardgame.exception.InvalidBoardGameException;
import trd.home.boardgame.service.BoardGameService;
import trd.home.frontend.FrontendPageRenderer;

@Controller
@RequestMapping("/boardgame")
@RequiredArgsConstructor
public class BoardGameFrontendController {
    private final BoardGameService service;
    private final FrontendPageRenderer pages;

    @ModelAttribute("states")
    public BoardGameState[] states() {
        return BoardGameState.values();
    }

    @ModelAttribute("types")
    public BoardGameType[] types() {
        return BoardGameType.values();
    }

    @GetMapping
    public String index() {
        return "redirect:/boardgame/games";
    }

    @GetMapping("/games")
    public String games(
            @ModelAttribute("filter") BoardGameSearchFilter filter,
            BindingResult binding,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("results", Page.empty());
        if (binding.hasErrors()) model.addAttribute("error", "Check the game search fields.");
        else {
            try {
                model.addAttribute("results", service.searchGames(filter, page));
            } catch (InvalidBoardGameException exception) {
                model.addAttribute("error", exception.getMessage());
            }
        }
        return pages.render(
                model, "/boardgame/games", "Board games", "Search and manage your collection.", "boardgame/games");
    }

    @GetMapping("/games/new")
    public String newGame(Model model) {
        model.addAttribute("form", new BoardGameInput("", "", BoardGameState.OWN, BoardGameType.BASE));
        return gameForm(model, null);
    }

    @GetMapping("/games/{id}/edit")
    public String editGame(@PathVariable String id, Model model) {
        var game = service.getGame(id);
        model.addAttribute("form", new BoardGameInput(game.name(), game.bggLink(), game.state(), game.type()));
        return gameForm(model, id);
    }

    @PostMapping("/games")
    public String createGame(
            @ModelAttribute("form") BoardGameInput input,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "Check the game fields.");
            return gameForm(model, null);
        }
        try {
            service.createGame(input);
        } catch (InvalidBoardGameException exception) {
            model.addAttribute("error", exception.getMessage());
            return gameForm(model, null);
        }
        redirect.addFlashAttribute("success", "Board game saved.");
        return "redirect:/boardgame/games";
    }

    @PostMapping("/games/{id}")
    public String updateGame(
            @PathVariable String id,
            @ModelAttribute("form") BoardGameInput input,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "Check the game fields.");
            return gameForm(model, id);
        }
        try {
            service.updateGame(id, input);
        } catch (InvalidBoardGameException | BoardGameNotFoundException exception) {
            model.addAttribute("error", exception.getMessage());
            return gameForm(model, id);
        }
        redirect.addFlashAttribute("success", "Board game updated.");
        return "redirect:/boardgame/games";
    }

    @GetMapping("/plays")
    public String plays(
            @ModelAttribute("filter") BoardGamePlaySearchFilter filter,
            BindingResult binding,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("results", Page.empty());
        if (binding.hasErrors()) model.addAttribute("error", "Check the play search fields.");
        else {
            try {
                model.addAttribute("results", service.searchPlays(filter, page));
            } catch (InvalidBoardGameException exception) {
                model.addAttribute("error", exception.getMessage());
            }
        }
        return pages.render(
                model,
                "/boardgame/plays",
                "Board game plays",
                "Search your plays by game, date and rating.",
                "boardgame/plays");
    }

    @GetMapping("/plays/new")
    public String newPlay(@RequestParam(required = false) String boardGameId, Model model) {
        model.addAttribute("form", new BoardGamePlayInput(boardGameId, null, null));
        return playForm(model);
    }

    @PostMapping("/plays")
    public String recordPlay(
            @ModelAttribute("form") BoardGamePlayInput input,
            BindingResult binding,
            Model model,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "Check the play date, game and integer rating.");
            return playForm(model);
        }
        try {
            service.recordPlay(input);
        } catch (InvalidBoardGameException | BoardGameNotFoundException exception) {
            model.addAttribute("error", exception.getMessage());
            return playForm(model);
        }
        redirect.addFlashAttribute("success", "Play recorded.");
        return "redirect:/boardgame/plays";
    }

    private String gameForm(Model model, String id) {
        model.addAttribute("gameId", id);
        return pages.render(
                model,
                id == null ? "/boardgame/games/new" : "/boardgame/games",
                id == null ? "Add board game" : "Edit board game",
                "Manage the details of a game.",
                "boardgame/game-form");
    }

    private String playForm(Model model) {
        model.addAttribute("games", service.gameChoices());
        return pages.render(
                model,
                "/boardgame/plays/new",
                "Record play",
                "Record when you played and rate the experience from 1 to 5.",
                "boardgame/play-form");
    }
}
