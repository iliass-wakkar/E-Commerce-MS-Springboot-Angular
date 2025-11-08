package com.Client.interceptor;

import com.Client.exception.ForbiddenException;
import com.Client.model.Role;
import com.Client.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object userAttr = request.getAttribute("authenticatedUser");

        if (userAttr instanceof User) {
            User user = (User) userAttr;
            if (user.getRole() == Role.ADMIN) {
                return true;
            }
        }

        throw new ForbiddenException("Requires ADMIN role.");
    }
}
