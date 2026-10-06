package com.example.chapter03daily.common.interceptor;

import com.example.chapter03daily.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class AuthenticatedUser implements Principal {

    private final User user;
    private final String name;

    public AuthenticatedUser(User user) {
        this.user = user;
        this.name = user.getName();
    }

    public static User fromPrincipal(Principal principal) {
        return ((AuthenticatedUser) principal).getUser();
    }
}
