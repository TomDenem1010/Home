package trd.home.frontend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dto.UserDto;
import trd.home.auth.service.AuthService;

class AuthFrontendControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthFrontendController controller = new AuthFrontendController(authService);

    @Test
    void authRedirectsToUserList() {
        assertEquals("redirect:/auth/users", controller.auth());
    }

    @Test
    void listsUsers() {
        var users = List.of(new UserDto("user-1", "alice", Set.of(UserRole.ADMIN)));
        var model = new ConcurrentModel();
        when(authService.getAllUsers()).thenReturn(users);

        assertEquals("index", controller.listUsers(model));
        assertEquals(users, model.getAttribute("users"));
        assertEquals("/auth/users", model.getAttribute("activePath"));
        assertEquals("auth/users", model.getAttribute("contentTemplate"));
    }

    @Test
    void showsCreateUserPageWithAvailableRoles() {
        var model = new ConcurrentModel();
        when(authService.getAvailableRoles()).thenReturn(Set.of(UserRole.ADMIN, UserRole.TCG));

        assertEquals("index", controller.createUser(model));
        assertEquals(Set.of(UserRole.ADMIN, UserRole.TCG), model.getAttribute("availableRoles"));
        assertEquals("/auth/create-user", model.getAttribute("activePath"));
        assertEquals("auth/create-user", model.getAttribute("contentTemplate"));
    }

    @Test
    void createsUserAndRedirectsToList() {
        assertEquals("redirect:/auth/users", controller.createUser("alice", "plain-password", Set.of(UserRole.TCG)));

        verify(authService).save("alice", "plain-password", Set.of(UserRole.TCG));
    }

    @Test
    void showsUpdateUserPageWithUsersAndRoles() {
        var users = List.of(new UserDto("user-1", "alice", Set.of(UserRole.TCG)));
        var model = new ConcurrentModel();
        when(authService.getAllUsers()).thenReturn(users);
        when(authService.getAvailableRoles()).thenReturn(Set.of(UserRole.ADMIN, UserRole.TCG));

        assertEquals("index", controller.updateRoles(model));
        assertEquals(users, model.getAttribute("users"));
        assertEquals(Set.of(UserRole.ADMIN, UserRole.TCG), model.getAttribute("availableRoles"));
        assertEquals("/auth/update-roles", model.getAttribute("activePath"));
        assertEquals("auth/update-roles", model.getAttribute("contentTemplate"));
    }

    @Test
    void updatesUserByIdAndRedirectsToList() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(authService.updateRoles("user-1", Set.of(UserRole.ADMIN)))
                .thenReturn(new UserDto("user-1", "alice", Set.of(UserRole.ADMIN)));

        assertEquals(
                "redirect:/auth/users",
                controller.updateRoles(
                        "user-1",
                        Set.of(UserRole.ADMIN),
                        authentication,
                        mock(HttpServletRequest.class),
                        mock(HttpServletResponse.class)));

        verify(authService).updateRoles("user-1", Set.of(UserRole.ADMIN));
    }

    @Test
    void logsCurrentUserOutAfterUpdatingOwnRoles() {
        Authentication authentication = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(authentication.getName()).thenReturn("alice");
        when(request.getSession(false)).thenReturn(session, (HttpSession) null);
        when(authService.updateRoles("user-1", Set.of(UserRole.ADMIN, UserRole.TCG)))
                .thenReturn(new UserDto("user-1", "alice", Set.of(UserRole.ADMIN, UserRole.TCG)));

        assertEquals(
                "redirect:/login",
                controller.updateRoles(
                        "user-1", Set.of(UserRole.ADMIN, UserRole.TCG), authentication, request, response));

        verify(session).invalidate();
    }

    @Test
    void showsUpdatePasswordPageWithUsers() {
        var users = List.of(new UserDto("user-1", "alice", Set.of(UserRole.TCG)));
        var model = new ConcurrentModel();
        when(authService.getAllUsers()).thenReturn(users);

        assertEquals("index", controller.updatePassword(model));
        assertEquals(users, model.getAttribute("users"));
        assertEquals("/auth/update-password", model.getAttribute("activePath"));
        assertEquals("auth/update-password", model.getAttribute("contentTemplate"));
    }

    @Test
    void updatesPasswordByUserIdAndRedirectsToList() {
        assertEquals("redirect:/auth/users", controller.updatePassword("user-1", "new-password"));

        verify(authService).updatePassword("user-1", "new-password");
    }
}
