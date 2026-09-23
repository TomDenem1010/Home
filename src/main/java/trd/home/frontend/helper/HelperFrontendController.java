package trd.home.frontend.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trd.home.tcg.service.TcgService;

@Controller
@RequestMapping("/helper")
@RequiredArgsConstructor
public class HelperFrontendController {

    private final TcgService tcgService;

    @PostMapping("/start-chrome")
    public String startChrome() {
        tcgService.startChrome();
        return "redirect:/";
    }
}
