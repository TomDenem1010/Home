package trd.home.auth.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dto.ApplicationLogSearchFilter;
import trd.home.auth.dto.ApplicationLogSearchResult;
import trd.home.auth.dto.UserDto;
import trd.home.auth.service.user.AuthOperationsService;
import trd.home.auth.validator.ApplicationLogSearchFilterValidator;
import trd.home.common.logging.LogMasked;
import trd.home.common.logging.LogMethodCall;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthOperationsService operations;
    private final ApplicationLogSearchService applicationLogSearchService;
    private final List<ApplicationLogSearchFilterValidator> applicationLogSearchFilterValidators;

    public List<String> getApplicationLogCreators() {
        return Stream.concat(operations.getAllUsers().stream().map(user -> user.username()), Stream.of("system"))
                .distinct()
                .sorted()
                .toList();
    }

    public Page<ApplicationLogSearchResult> searchApplicationLogs(
            ApplicationLogSearchFilter filter, int page, int size) {
        applicationLogSearchFilterValidators.forEach(validator -> validator.validate(filter, page, size));
        return applicationLogSearchService.search(filter, page, size);
    }

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
