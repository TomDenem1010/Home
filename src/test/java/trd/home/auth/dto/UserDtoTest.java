package trd.home.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;

class UserDtoTest {

    @Test
    void mapsUserAndCopiesRoles() {
        User user = new User();
        user.setId("user-id");
        user.setUsername("alice");
        user.setRoles(Set.of(UserRole.ADMIN));

        UserDto dto = UserDto.from(user);

        assertEquals("user-id", dto.id());
        assertEquals("alice", dto.username());
        assertEquals(Set.of(UserRole.ADMIN), dto.roles());
        assertThrows(UnsupportedOperationException.class, () -> dto.roles().clear());
    }
}
