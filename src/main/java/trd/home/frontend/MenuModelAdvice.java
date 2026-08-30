package trd.home.frontend;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import trd.home.common.logging.LogMethodCall;

@ControllerAdvice
public class MenuModelAdvice {

    @ModelAttribute("menuItems")
    @LogMethodCall
    public List<MenuItem> menuItems(Authentication authentication) {
        boolean admin = hasRole(authentication, "ADMIN");
        boolean tcg = hasRole(authentication, "TCG");

        return List.of(
                new MenuItem(
                        "Auth",
                        admin,
                        List.of(
                                new SubmenuItem("List users", "/auth/users", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem("Create user", "/auth/create-user", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem("Update roles", "/auth/update-roles", SubmenuItem.Type.PAGE, admin),
                                new SubmenuItem(
                                        "Update password", "/auth/update-password", SubmenuItem.Type.PAGE, admin))),
                new MenuItem(
                        "TCG",
                        tcg,
                        List.of(
                                new SubmenuItem(
                                        "Save Deck From Resource",
                                        "/tcg/save-decks-from-resource",
                                        SubmenuItem.Type.ACTION,
                                        tcg),
                                new SubmenuItem(
                                        "Refresh Deck Prices",
                                        "/tcg/refresh-deck-prices",
                                        SubmenuItem.Type.ACTION,
                                        tcg),
                                new SubmenuItem("Statistics", "/tcg/statistics", SubmenuItem.Type.PAGE, tcg))));
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
