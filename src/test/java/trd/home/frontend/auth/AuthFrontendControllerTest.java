package trd.home.frontend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dto.UserDto;
import trd.home.auth.service.UserService;

class AuthFrontendControllerTest {

    private final UserService userService = mock(UserService.class);
    private final AuthFrontendController controller = new AuthFrontendController(userService);

    @Test
    void authRedirectsToUserList() {
        assertEquals("redirect:/auth/users", controller.auth());
    }

    @Test
    void listsUsers() {
        var users = List.of(new UserDto("user-1", "alice", Set.of(UserRole.ADMIN)));
        var model = new ConcurrentModel();
        when(userService.getAllUsers()).thenReturn(users);

        assertEquals("index", controller.listUsers(model));
        assertEquals(users, model.getAttribute("users"));
        assertEquals("/auth/users", model.getAttribute("activePath"));
        assertEquals("auth/users", model.getAttribute("contentTemplate"));
    }

    @Test
    void showsCreateUserPageWithAvailableRoles() {
        var model = new ConcurrentModel();
        when(userService.getAvailableRoles()).thenReturn(Set.of(UserRole.ADMIN, UserRole.TCG));

        assertEquals("index", controller.createUser(model));
        assertEquals(Set.of(UserRole.ADMIN, UserRole.TCG), model.getAttribute("availableRoles"));
        assertEquals("/auth/create-user", model.getAttribute("activePath"));
        assertEquals("auth/create-user", model.getAttribute("contentTemplate"));
    }

    @Test
    void createsUserAndRedirectsToList() {
        assertEquals("redirect:/auth/users", controller.createUser("alice", "plain-password", Set.of(UserRole.TCG)));

        verify(userService).save("alice", "plain-password", Set.of(UserRole.TCG));
    }

    @Test
    void showsUpdateUserPageWithUsersAndRoles() {
        var users = List.of(new UserDto("user-1", "alice", Set.of(UserRole.TCG)));
        var model = new ConcurrentModel();
        when(userService.getAllUsers()).thenReturn(users);
        when(userService.getAvailableRoles()).thenReturn(Set.of(UserRole.ADMIN, UserRole.TCG));

        assertEquals("index", controller.updateRoles(model));
        assertEquals(users, model.getAttribute("users"));
        assertEquals(Set.of(UserRole.ADMIN, UserRole.TCG), model.getAttribute("availableRoles"));
        assertEquals("/auth/update-roles", model.getAttribute("activePath"));
        assertEquals("auth/update-roles", model.getAttribute("contentTemplate"));
    }

    @Test
    void updatesUserByIdAndRedirectsToList() {
        assertEquals("redirect:/auth/users", controller.updateRoles("user-1", Set.of(UserRole.ADMIN)));

        verify(userService).updateRoles("user-1", Set.of(UserRole.ADMIN));
    }

    @Test
    void showsUpdatePasswordPageWithUsers() {
        var users = List.of(new UserDto("user-1", "alice", Set.of(UserRole.TCG)));
        var model = new ConcurrentModel();
        when(userService.getAllUsers()).thenReturn(users);

        assertEquals("index", controller.updatePassword(model));
        assertEquals(users, model.getAttribute("users"));
        assertEquals("/auth/update-password", model.getAttribute("activePath"));
        assertEquals("auth/update-password", model.getAttribute("contentTemplate"));
    }

    @Test
    void updatesPasswordByUserIdAndRedirectsToList() {
        assertEquals("redirect:/auth/users", controller.updatePassword("user-1", "new-password"));

        verify(userService).updatePassword("user-1", "new-password");
    }
}
