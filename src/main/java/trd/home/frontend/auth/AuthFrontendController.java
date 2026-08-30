package trd.home.frontend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dto.UserDto;
import trd.home.auth.service.AuthService;
import trd.home.common.logging.LogMasked;
import trd.home.common.logging.LogMethodCall;

@Controller
@RequestMapping("/auth")
public class AuthFrontendController {

    private final AuthService authService;

    public AuthFrontendController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @LogMethodCall
    public String auth() {
        return "redirect:/auth/users";
    }

    @GetMapping("/users")
    @LogMethodCall
    public String listUsers(Model model) {
        model.addAttribute("users", authService.getAllUsers());
        return renderPage(model, "/auth/users", "Users", "Registered users and their roles.", "auth/users");
    }

    @GetMapping("/create-user")
    @LogMethodCall
    public String createUser(Model model) {
        model.addAttribute("availableRoles", authService.getAvailableRoles());
        return renderPage(
                model, "/auth/create-user", "Create user", "Create a new user and assign roles.", "auth/create-user");
    }

    @PostMapping("/create-user")
    @LogMethodCall
    public String createUser(
            @RequestParam String username,
            @RequestParam @LogMasked String password,
            @RequestParam(required = false) Set<UserRole> roles) {
        authService.save(username, password, roles == null ? Set.of() : roles);
        return "redirect:/auth/users";
    }

    @GetMapping("/update-roles")
    @LogMethodCall
    public String updateRoles(Model model) {
        model.addAttribute("users", authService.getAllUsers());
        model.addAttribute("availableRoles", authService.getAvailableRoles());
        return renderPage(
                model, "/auth/update-roles", "Update roles", "Update a user's roles by user ID.", "auth/update-roles");
    }

    @PostMapping("/update-roles")
    @LogMethodCall
    public String updateRoles(
            @RequestParam String userId,
            @RequestParam(required = false) Set<UserRole> roles,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        UserDto updatedUser = authService.updateRoles(userId, roles == null ? Set.of() : roles);
        if (updatedUser.username().equals(authentication.getName())) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login";
        }
        return "redirect:/auth/users";
    }

    @GetMapping("/update-password")
    @LogMethodCall
    public String updatePassword(Model model) {
        model.addAttribute("users", authService.getAllUsers());
        return renderPage(
                model,
                "/auth/update-password",
                "Update password",
                "Update a user's password by user ID.",
                "auth/update-password");
    }

    @PostMapping("/update-password")
    @LogMethodCall
    public String updatePassword(@RequestParam String userId, @RequestParam @LogMasked String password) {
        authService.updatePassword(userId, password);
        return "redirect:/auth/users";
    }

    private String renderPage(Model model, String activePath, String title, String content, String contentTemplate) {
        model.addAttribute("activePath", activePath);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageContent", content);
        model.addAttribute("contentTemplate", contentTemplate);
        return "index";
    }
}
