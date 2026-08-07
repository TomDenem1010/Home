package trd.home.frontend.tcg;

import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import trd.home.frontend.MenuItem;
import trd.home.frontend.SubmenuItem;

@ControllerAdvice
public class TcgMenuModelAdvice {

    private static final boolean AUTHORIZED = true;
    private static final List<MenuItem> MENU_ITEMS = List.of(new MenuItem(
            "TCG",
            AUTHORIZED,
            List.of(
                    new SubmenuItem(
                            "Save Deck From Resource",
                            "/tcg/save-decks-from-resource",
                            SubmenuItem.Type.ACTION,
                            AUTHORIZED),
                    new SubmenuItem(
                            "Refresh Deck Prices", "/tcg/refresh-deck-prices", SubmenuItem.Type.ACTION, AUTHORIZED),
                    new SubmenuItem("Statistics", "/tcg/statistics", SubmenuItem.Type.PAGE, AUTHORIZED))));

    @ModelAttribute("menuItems")
    public List<MenuItem> menuItems() {
        return MENU_ITEMS;
    }
}
