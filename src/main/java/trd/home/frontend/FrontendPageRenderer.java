package trd.home.frontend;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class FrontendPageRenderer {

    private static final String INDEX_VIEW = "index";

    public String render(Model model, String activePath, String title, String description) {
        return render(model, activePath, title, description, null, null);
    }

    public String render(Model model, String activePath, String title, String description, String contentTemplate) {
        return render(model, activePath, title, description, contentTemplate, null);
    }

    public String render(
            Model model,
            String activePath,
            String title,
            String description,
            String contentTemplate,
            String featureName) {
        model.addAttribute("activePath", activePath);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageContent", description);
        if (contentTemplate != null) {
            model.addAttribute("contentTemplate", contentTemplate);
        }
        if (featureName != null) {
            model.addAttribute("featureStylesheet", "/css/" + featureName + ".css");
            model.addAttribute("featureScript", "/js/" + featureName + ".js");
        }
        return INDEX_VIEW;
    }
}
