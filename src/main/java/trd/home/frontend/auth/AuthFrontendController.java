package trd.home.frontend.auth;

import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import trd.home.auth.constant.UserRole;
import trd.home.auth.service.UserService;

@Controller
@RequestMapping("/auth")
public class AuthFrontendController {

    private final UserService userService;

    public AuthFrontendController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String auth() {
        return "redirect:/auth/users";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return renderPage(model, "/auth/users", "Users", "Registered users and their roles.", "auth/users");
    }

    @GetMapping("/create-user")
    public String createUser(Model model) {
        model.addAttribute("availableRoles", userService.getAvailableRoles());
        return renderPage(
                model, "/auth/create-user", "Create user", "Create a new user and assign roles.", "auth/create-user");
    }

    @PostMapping("/create-user")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) Set<UserRole> roles) {
        userService.save(username, password, roles == null ? Set.of() : roles);
        return "redirect:/auth/users";
    }

    @GetMapping("/update-roles")
    public String updateUser(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("availableRoles", userService.getAvailableRoles());
        return renderPage(
                model, "/auth/update-roles", "Update roles", "Update a user's roles by user ID.", "auth/update-user");
    }

    @PostMapping("/update-user")
    public String updateUser(@RequestParam String userId, @RequestParam(required = false) Set<UserRole> roles) {
        userService.updateRoles(userId, roles == null ? Set.of() : roles);
        return "redirect:/auth/users";
    }

    @GetMapping("/update-password")
    public String updatePassword(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return renderPage(
                model,
                "/auth/update-password",
                "Update password",
                "Update a user's password by user ID.",
                "auth/update-password");
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String userId, @RequestParam String password) {
        userService.updatePassword(userId, password);
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
