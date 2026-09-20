package trd.home.frontend.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trd.home.common.browser.ChromeBrowserLauncher;
import trd.home.common.logging.LogMethodCall;

@Controller
@RequestMapping("/helper")
@RequiredArgsConstructor
public class HelperFrontendController {

    private final ChromeBrowserLauncher chromeBrowserLauncher;

    @PostMapping("/start-chrome")
    @LogMethodCall
    public String startChrome() {
        chromeBrowserLauncher.start();
        return "redirect:/";
    }
}
