package trd.home.frontend;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    private static final List<MenuItem> MENU_ITEMS =
            List.of(new MenuItem("Kezdőlap", "/"), new MenuItem("TCG", "/tcg"));

    @GetMapping("/")
    public String home(Model model) {
        return renderPage(model, "/", "Kezdőlap", "Üdvözöllek az alkalmazásban!");
    }

    @GetMapping("/tcg")
    public String tcg(Model model) {
        return renderPage(model, "/tcg", "TCG", "A TCG funkciók hamarosan itt lesznek elérhetők.");
    }

    private String renderPage(Model model, String activePath, String title, String content) {
        model.addAttribute("menuItems", MENU_ITEMS);
        model.addAttribute("activePath", activePath);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageContent", content);
        return "index";
    }
}
