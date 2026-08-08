package trd.home.frontend;

import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class MenuModelAdvice {

    private static final boolean AUTHORIZED = true;
    private static final List<MenuItem> MENU_ITEMS = List.of(
            new MenuItem(
                    "Auth",
                    AUTHORIZED,
                    List.of(
                            new SubmenuItem("List users", "/auth/users", SubmenuItem.Type.PAGE, AUTHORIZED),
                            new SubmenuItem("Create user", "/auth/create-user", SubmenuItem.Type.PAGE, AUTHORIZED),
                            new SubmenuItem("Update user", "/auth/update-user", SubmenuItem.Type.PAGE, AUTHORIZED))),
            new MenuItem(
                    "TCG",
                    AUTHORIZED,
                    List.of(
                            new SubmenuItem(
                                    "Save Deck From Resource",
                                    "/tcg/save-decks-from-resource",
                                    SubmenuItem.Type.ACTION,
                                    AUTHORIZED),
                            new SubmenuItem(
                                    "Refresh Deck Prices",
                                    "/tcg/refresh-deck-prices",
                                    SubmenuItem.Type.ACTION,
                                    AUTHORIZED),
                            new SubmenuItem("Statistics", "/tcg/statistics", SubmenuItem.Type.PAGE, AUTHORIZED))));

    @ModelAttribute("menuItems")
    public List<MenuItem> menuItems() {
        return MENU_ITEMS;
    }
}
