# Files to Move/Create in Gateway Microservice

## DTOs to Move from Client-MS to Gateway

These DTOs are no longer used in Client-MS and should be moved to the Gateway project:

### 1. LoginRequestDTO.java
```java
package com.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String email;
    private String password;
}
```

**Usage**: Gateway will receive login requests with this DTO.

---

### 2. LoginResponseDTO.java
```java
package com.gateway.dto;

import com.Client.dto.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UserResponseDTO user;
}
```

**Usage**: Gateway will send this response after successful login.

---

## New Classes to Create in Gateway

### 3. AuthService.java (Interface)
```java
package com.gateway.service;

import com.gateway.dto.LoginRequestDTO;
import com.gateway.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    void logout(String token);
    Long getUserIdFromToken(String token);
    boolean isValidToken(String token);
    boolean isAdmin(String token);
}
```

---

### 4. AuthServiceImpl.java
```java
package com.gateway.service;

import com.Client.dto.UserResponseDTO;
import com.Client.model.Role;
import com.Client.model.User;
import com.gateway.client.UserClient;
import com.gateway.dto.LoginRequestDTO;
import com.gateway.dto.LoginResponseDTO;
import com.gateway.exception.AuthException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    
    // In-memory token store
    // In production, use Redis or a database
    private final Map<String, Long> activeTokens = new ConcurrentHashMap<>();
    private final Map<String, User> tokenToUser = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserClient userClient, PasswordEncoder passwordEncoder) {
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Call Client-MS to get user by email
        User user = userClient.getUserByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new AuthException("Invalid email or password."));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid email or password.");
        }

        // Generate token
        String token = UUID.randomUUID().toString();
        
        // Store token mapping
        activeTokens.put(token, user.getId());
        tokenToUser.put(token, user);

        // Convert to UserResponseDTO (without password)
        UserResponseDTO userResponse = convertToResponseDTO(user);

        return new LoginResponseDTO(token, userResponse);
    }

    @Override
    public void logout(String token) {
        tokenToUser.remove(token);
        activeTokens.remove(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return activeTokens.get(token);
    }

    @Override
    public boolean isValidToken(String token) {
        return activeTokens.containsKey(token);
    }

    @Override
    public boolean isAdmin(String token) {
        User user = tokenToUser.get(token);
        return user != null && user.getRole() == Role.ADMIN;
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole(),
            user.getShippingAddress(),
            user.getPhone(),
            user.getCreatedAt()
        );
    }
}
```

---

### 5. UserClient.java (Feign Client)
```java
package com.gateway.client;

import com.Client.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "client-service", path = "/api/v1/users")
public interface UserClient {
    
    @GetMapping("/email/{email}")
    Optional<User> getUserByEmail(@PathVariable String email);
}
```

---

### 6. AuthController.java
```java
package com.gateway.controller;

import com.gateway.dto.LoginRequestDTO;
import com.gateway.dto.LoginResponseDTO;
import com.gateway.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }
}
```

---

### 7. AuthException.java
```java
package com.gateway.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
```

---

### 8. ForbiddenException.java
```java
package com.gateway.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
```

---

### 9. GlobalExceptionHandler.java (Gateway)
```java
package com.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleAuthException(AuthException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

---

### 10. AuthFilter.java (Gateway Filter)
```java
package com.gateway.filter;

import com.gateway.exception.AuthException;
import com.gateway.exception.ForbiddenException;
import com.gateway.service.AuthService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final AuthService authService;

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/users"  // POST for registration
    );

    // Paths that require admin role
    private static final List<String> ADMIN_PATHS = List.of(
        "/api/v1/admin"
    );

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // Allow public paths
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // Validate token
        if (!authService.isValidToken(token)) {
            throw new AuthException("Invalid or expired token");
        }

        // Check admin access for admin paths
        if (isAdminPath(path) && !authService.isAdmin(token)) {
            throw new ForbiddenException("Access denied. Admin role required.");
        }

        // Add user ID to request headers for downstream services
        Long userId = authService.getUserIdFromToken(token);
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", userId.toString())
            .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String path, String method) {
        if (path.equals("/api/v1/users") && method.equals("POST")) {
            return true; // Registration
        }
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
```

---

### 11. SecurityConfig.java (Gateway)
```java
package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Gateway Dependencies (pom.xml)

Add these dependencies to the Gateway's pom.xml:

```xml
<!-- Spring Cloud Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- Eureka Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Spring Security (for BCrypt) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## Gateway Routes Configuration (application.yml)

```yaml
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        # Auth routes
        - id: auth-service
          uri: lb://gateway-service
          predicates:
            - Path=/api/v1/auth/**
          
        # Client-MS routes
        - id: client-service
          uri: lb://client-service
          predicates:
            - Path=/api/v1/users/**,/api/v1/admin/**

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## Testing the Gateway

### 1. Register a new user
```bash
POST http://localhost:8080/api/v1/users
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "shippingAddress": "123 Main St",
  "phone": "+1234567890"
}
```

### 2. Login
```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "uuid-token-here",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "role": "CLIENT",
    "shippingAddress": "123 Main St",
    "phone": "+1234567890"
  }
}
```

### 3. Access protected endpoint
```bash
GET http://localhost:8080/api/v1/users/1
Authorization: Bearer uuid-token-here
```

### 4. Logout
```bash
POST http://localhost:8080/api/v1/auth/logout
Authorization: Bearer uuid-token-here
```

---

## Migration Checklist

- [ ] Create Gateway Spring Boot project
- [ ] Add required dependencies (Gateway, Eureka, Feign, Security)
- [ ] Move LoginRequestDTO and LoginResponseDTO from Client-MS to Gateway
- [ ] Create AuthService and AuthServiceImpl
- [ ] Create UserClient (Feign)
- [ ] Create AuthController
- [ ] Create AuthException and ForbiddenException
- [ ] Create GlobalExceptionHandler
- [ ] Create AuthFilter for Gateway
- [ ] Create SecurityConfig with PasswordEncoder
- [ ] Configure Gateway routes in application.yml
- [ ] Register Gateway with Eureka
- [ ] Test authentication flow
- [ ] Test authorization (admin vs client)
- [ ] Test token validation
- [ ] Test logout functionality

---

## Notes

1. **Token Storage**: Currently using in-memory storage. For production:
   - Use Redis for distributed token storage
   - Or implement JWT tokens (stateless)

2. **Password Verification**: Gateway needs BCrypt to verify passwords against the hashed passwords from Client-MS.

3. **User Data**: Gateway fetches full User entity (including password) from Client-MS, verifies password, then returns UserResponseDTO (without password) to client.

4. **Header Injection**: Gateway adds `X-User-Id` header to requests forwarded to Client-MS, so downstream services know which user is making the request.

5. **Role-Based Access**: Gateway checks user role before allowing access to admin endpoints.

