package com.Client.interceptor;

import com.Client.exception.AuthException;
import com.Client.model.User;
import com.Client.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            throw new AuthException("Missing or invalid Authorization header.");
        }

        String base64Credentials = authorizationHeader.substring("Basic ".length());
        byte[] credDecoded;
        try {
            credDecoded = Base64.getDecoder().decode(base64Credentials);
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid Base64 format.");
        }

        String credentials = new String(credDecoded);
        final String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            throw new AuthException("Invalid credentials format.");
        }

        String email = values[0];
        String password = values[1];

        Optional<User> userOptional = userService.authenticate(email, password);

        if (userOptional.isPresent()) {
            request.setAttribute("authenticatedUser", userOptional.get());
            return true;
        }

        throw new AuthException("Invalid email or password.");
    }
}
