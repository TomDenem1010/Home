package trd.home.auth.dto;

import java.util.Set;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.common.logging.LogMethodCall;

public record UserDto(String id, String username, Set<UserRole> roles) {

    @LogMethodCall
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), Set.copyOf(user.getRoles()));
    }
}
