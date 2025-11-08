package com.Client.config;

import com.Client.interceptor.AdminInterceptor;
import com.Client.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, AdminInterceptor adminInterceptor) {
        this.authInterceptor = authInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // AuthInterceptor protects all /me and /admin endpoints
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/users/me", "/api/v1/admin/**");

        // AdminInterceptor runs after AuthInterceptor and only protects /admin endpoints
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/v1/admin/**");
    }
}
