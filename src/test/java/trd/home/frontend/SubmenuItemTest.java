package trd.home.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class SubmenuItemTest {

    @Test
    void exposesAllMenuProperties() {
        SubmenuItem item = new SubmenuItem("Library", "/media", SubmenuItem.Type.PAGE, false);

        assertEquals("Library", item.label());
        assertEquals("/media", item.path());
        assertEquals(SubmenuItem.Type.PAGE, item.type());
        assertFalse(item.authorized());
    }
}
