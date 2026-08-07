package trd.home.frontend;

public record SubmenuItem(String label, String path, Type type, boolean authorized) {

    public enum Type {
        ACTION,
        PAGE
    }
}
