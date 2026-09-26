package trd.home.frontend.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trd.home.common.browser.ChromeBrowserLauncher;
import trd.home.frontend.FrontendPageRenderer;

@Controller
@RequestMapping("/helper")
public class HelperFrontendController {

    private final ChromeBrowserLauncher chromeBrowserLauncher;
    private final FrontendPageRenderer pageRenderer;
    private final String desktopUrl;

    public HelperFrontendController(
            ChromeBrowserLauncher chromeBrowserLauncher,
            FrontendPageRenderer pageRenderer,
            @Value("${chrome.desktop-url:}") String desktopUrl) {
        this.chromeBrowserLauncher = chromeBrowserLauncher;
        this.pageRenderer = pageRenderer;
        this.desktopUrl = desktopUrl;
    }

    @PostMapping("/start-chrome")
    public String startChrome() {
        chromeBrowserLauncher.start();
        return desktopUrl.isBlank() ? "redirect:/" : "redirect:/helper/chrome";
    }

    @GetMapping("/chrome")
    public String chrome(Model model) {
        model.addAttribute("chromeDesktopUrl", desktopUrl);
        return pageRenderer.render(
                model,
                "/helper/chrome",
                "Chrome",
                "Sign in to the website here before refreshing prices.",
                "helper/chrome");
    }
}
