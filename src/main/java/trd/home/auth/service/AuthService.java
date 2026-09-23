package trd.home.auth.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dto.UserDto;
import trd.home.auth.service.user.AuthOperationsService;
import trd.home.common.logging.LogMasked;
import trd.home.common.logging.LogMethodCall;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthOperationsService operations;

    @LogMethodCall
    public Set<UserRole> getAvailableRoles() {
        return operations.getAvailableRoles();
    }

    @LogMethodCall
    public List<UserDto> getAllUsers() {
        return operations.getAllUsers();
    }

    @LogMethodCall
    public UserDto save(String username, @LogMasked String password, Set<UserRole> roles) {
        return operations.save(username, password, roles);
    }

    @LogMethodCall
    public UserDto updateRoles(String userId, Set<UserRole> roles) {
        return operations.updateRoles(userId, roles);
    }

    @LogMethodCall
    public UserDto updatePassword(String userId, @LogMasked String password) {
        return operations.updatePassword(userId, password);
    }
}
