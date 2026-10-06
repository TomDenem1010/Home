package trd.home.frontend;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class MenuModelAdvice {

    private final String frontendEventHistoryScope = UUID.randomUUID().toString();

    @ModelAttribute("frontendEventHistoryScope")
    public String frontendEventHistoryScope() {
        return frontendEventHistoryScope;
    }

    @ModelAttribute("menuItems")
    public List<MenuItem> menuItems(Authentication authentication) {
        boolean admin = hasRole(authentication, "ADMIN");
        boolean tcg = hasRole(authentication, "TCG");
        boolean media = hasRole(authentication, "MEDIA");
        boolean helper = authentication != null && authentication.isAuthenticated();

        return List.of(
                new MenuItem(
                        "AUTH",
                        admin,
                        List.of(
                                new SubmenuItem("list users", "/auth/users", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem("create user", "/auth/create-user", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem("update roles", "/auth/update-roles", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem(
                                        "update password", "/auth/update-password", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem(
                                        "application logs", "/auth/application-logs", SubmenuItem.Type.PAGE, admin))),
                new MenuItem(
                        "TCG",
                        tcg,
                        List.of(
                                new SubmenuItem("deck operations", "/tcg/deck-operations", SubmenuItem.Type.PAGE, tcg),
                                new SubmenuItem("statistics", "/tcg/statistics", SubmenuItem.Type.PAGE, tcg),
                                new SubmenuItem("versions", "/tcg/version", SubmenuItem.Type.PAGE, tcg),
                                new SubmenuItem("search card", "/tcg/search-card", SubmenuItem.Type.PAGE, tcg))),
                new MenuItem(
                        "MEDIA",
                        media,
                        List.of(
                                new SubmenuItem(
                                        "server folder path",
                                        "/media/server-folder-path",
                                        SubmenuItem.Type.PAGE,
                                        media),
                                new SubmenuItem("by actor", "/media/by-actor", SubmenuItem.Type.PAGE, media),
                                new SubmenuItem("by folder", "/media/by-folder", SubmenuItem.Type.PAGE, media))),
                new MenuItem(
                        "HELPER",
                        helper,
                        List.of(
                                new SubmenuItem(
                                        "start chrome", "/helper/start-chrome", SubmenuItem.Type.ACTION, helper),
                                new SubmenuItem("open chrome", "/helper/chrome", SubmenuItem.Type.PAGE, helper))));
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
