package trd.home.frontend;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class MenuModelAdviceTest {

    private final MenuModelAdvice advice = new MenuModelAdvice();

    @Test
    void adminCanOnlySeeAuthMenu() {
        var menus = advice.menuItems(authentication("ROLE_ADMIN"));
        var authMenu = menu(menus, "Auth");
        var tcgMenu = menu(menus, "TCG");

        assertAll(
                () -> assertTrue(authMenu.authorized()),
                () -> assertFalse(tcgMenu.authorized()),
                () -> assertTrue(authMenu.submenuItems().stream().allMatch(SubmenuItem::authorized)),
                () -> assertTrue(tcgMenu.submenuItems().stream().noneMatch(SubmenuItem::authorized)));
    }

    @Test
    void tcgUserCanOnlySeeTcgMenu() {
        var menus = advice.menuItems(authentication("ROLE_TCG"));
        var authMenu = menu(menus, "Auth");
        var tcgMenu = menu(menus, "TCG");

        assertAll(
                () -> assertFalse(authMenu.authorized()),
                () -> assertTrue(tcgMenu.authorized()),
                () -> assertTrue(authMenu.submenuItems().stream().noneMatch(SubmenuItem::authorized)),
                () -> assertTrue(tcgMenu.submenuItems().stream().allMatch(SubmenuItem::authorized)));
    }

    @Test
    void userWithAdminAndTcgRolesCanSeeBothMenus() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_TCG")));
        var menus = advice.menuItems(authentication);

        assertAll(
                () -> assertTrue(menu(menus, "Auth").authorized()),
                () -> assertTrue(menu(menus, "TCG").authorized()));
    }

    @Test
    void unauthenticatedUserCannotSeeProtectedMenus() {
        var menus = advice.menuItems(null);

        assertAll(
                () -> assertFalse(menu(menus, "Auth").authorized()),
                () -> assertFalse(menu(menus, "TCG").authorized()),
                () -> assertTrue(menus.stream()
                        .flatMap(item -> item.submenuItems().stream())
                        .noneMatch(SubmenuItem::authorized)));
    }

    @Test
    void unknownRoleCannotSeeProtectedMenus() {
        var menus = advice.menuItems(authentication("ROLE_UNKNOWN"));

        assertTrue(menus.stream().noneMatch(MenuItem::authorized));
    }

    @Test
    void createsExpectedMenuStructure() {
        var menus = advice.menuItems(authentication("ROLE_ADMIN"));

        assertEquals(2, menus.size());
        assertEquals(
                List.of("/auth/users", "/auth/create-user", "/auth/update-roles", "/auth/update-password"),
                menu(menus, "Auth").submenuItems().stream()
                        .map(SubmenuItem::path)
                        .toList());
        assertEquals(
                List.of("/tcg/save-decks-from-resource", "/tcg/refresh-deck-prices", "/tcg/statistics"),
                menu(menus, "TCG").submenuItems().stream()
                        .map(SubmenuItem::path)
                        .toList());
        assertEquals(
                List.of(SubmenuItem.Type.ACTION, SubmenuItem.Type.ACTION, SubmenuItem.Type.PAGE),
                menu(menus, "TCG").submenuItems().stream()
                        .map(SubmenuItem::type)
                        .toList());
    }

    private static UsernamePasswordAuthenticationToken authentication(String authority) {
        return new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority(authority)));
    }

    private static MenuItem menu(List<MenuItem> menus, String label) {
        return menus.stream()
                .filter(menu -> menu.label().equals(label))
                .findFirst()
                .orElseThrow();
    }
}
