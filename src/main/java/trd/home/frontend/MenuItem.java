package trd.home.frontend;

import java.util.List;
import lombok.NonNull;

public record MenuItem(
        @NonNull String label, boolean authorized, @NonNull List<SubmenuItem> submenuItems) {}
