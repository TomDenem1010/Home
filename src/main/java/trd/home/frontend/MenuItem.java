package trd.home.frontend;

import java.util.List;

public record MenuItem(String label, boolean authorized, List<SubmenuItem> submenuItems) {}
