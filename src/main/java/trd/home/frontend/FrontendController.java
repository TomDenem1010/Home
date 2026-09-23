package trd.home.frontend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class FrontendController {

    private final FrontendPageRenderer pageRenderer;

    @GetMapping("/")
    public String home(Model model) {
        return pageRenderer.render(model, "/", "Kezdőlap", "Üdvözöllek az alkalmazásban!");
    }
}
