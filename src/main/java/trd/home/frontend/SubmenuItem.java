package trd.home.frontend;

import lombok.NonNull;

public record SubmenuItem(
        @NonNull String label,
        @NonNull String path,
        @NonNull Type type,
        boolean authorized) {

    public enum Type {
        ACTION,
        PAGE
    }
}
