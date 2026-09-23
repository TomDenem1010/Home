package trd.home.auth.dto;

import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.common.logging.LogMethodCall;

public record UserDto(
        @NonNull String id,
        @NonNull String username,
        @NonNull Set<UserRole> roles) {

    @LogMethodCall
    public static UserDto from(User user) {
        return new UserDto(
                Objects.requireNonNullElse(user.getId(), ""),
                Objects.requireNonNullElse(user.getUsername(), ""),
                user.getRoles() == null ? Set.of() : Set.copyOf(user.getRoles()));
    }
}
