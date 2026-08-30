package trd.home.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import trd.home.common.logging.LogMethodCall;

@Controller
public class FrontendController {

    @GetMapping("/")
    @LogMethodCall
    public String home(Model model) {
        model.addAttribute("activePath", "/");
        model.addAttribute("pageTitle", "Kezdőlap");
        model.addAttribute("pageContent", "Üdvözöllek az alkalmazásban!");
        return "index";
    }
}
