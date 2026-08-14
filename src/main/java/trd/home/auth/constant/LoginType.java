package trd.home.auth.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoginType {
    LOGIN_SUCCESS("LOGIN"),
    LOGIN_FAILURE("LOGIN"),
    LOGOUT_SUCCESS("LOGOUT");

    private String path;
}
