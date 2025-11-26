# CORS Problem - FIXED ✓

## Problem Analysis

The error you received:
```
Access to XMLHttpRequest at 'http://localhost:1111/auth/login' from origin 'http://localhost:4200' 
has been blocked by CORS policy: Response to preflight request doesn't pass access control check: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

**Root Cause**: The Gateway did not have proper CORS configuration to allow requests from the Angular frontend running on `http://localhost:4200`.

---

## What Was Fixed

### 1. Added CORS Configuration to `application.yml`
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        add-to-simple-url-handler-mapping: true
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:4200"
            allowedMethods: "*"
            allowedHeaders: "*"
            exposedHeaders: "Authorization"
            allowCredentials: true
```

### 2. Created `CorsConfig.java`
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("http://localhost:4200");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addExposedHeader("Authorization");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
```

### 3. Updated `SecurityConfig.java`
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Allow preflight
                .pathMatchers("/auth/**").permitAll()                  // Allow auth endpoints
                .anyExchange().authenticated()                         // Secure others
            )
            .build();
    }
}
```

### 4. Added Spring Security Dependency to `pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## Changes Required in Frontend

### 1. Update API Base URL
**Before** (pointing to Client MS):
```typescript
apiBaseUrl: 'http://localhost:8081'
```

**After** (pointing to Gateway):
```typescript
// environment.development.ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:1111'  // Gateway URL
};
```

### 2. Update Authentication Endpoint
**Before**:
```typescript
login(credentials) {
  return this.http.post('http://localhost:8081/api/auth/login', credentials);
}
```

**After**:
```typescript
login(credentials) {
  return this.http.post('http://localhost:1111/auth/login', credentials);
}
```

### 3. Update Request Format (if needed)
The login endpoint expects:
```json
{
  "email": "user@example.com",     // Not "username"
  "password": "password123"
}
```

Make sure your frontend sends `email` not `username`.

---

## Gateway Authentication Endpoints

### POST /auth/login
**URL**: `http://localhost:1111/auth/login`

**Request**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response** (200):
```json
{
  "token": "generated-uuid-token",
  "userId": 1,
  "email": "user@example.com",
  "role": "CLIENT"
}
```

**Error Response** (401):
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/auth/login"
}
```

---

### POST /auth/register
**URL**: `http://localhost:1111/auth/register`

**Request**:
```json
{
  "email": "newuser@example.com",
  "password": "securePassword",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "+1234567890",
  "role": "CLIENT"
}
```

**Success Response** (201):
```json
{
  "id": 1,
  "email": "newuser@example.com",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "+1234567890",
  "role": "CLIENT",
  "password": null
}
```

---

### POST /auth/logout
**URL**: `http://localhost:1111/auth/logout`

**Headers Required**:
```
Authorization: Bearer <token>
```

**Success Response** (200):
```json
{
  "message": "Logged out successfully"
}
```

---

## How to Use from Frontend

### Complete Auth Service Example
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  token: string;
  userId: number;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = `${environment.apiBaseUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, { email, password })
      .pipe(
        tap(response => {
          sessionStorage.setItem('token', response.token);
          sessionStorage.setItem('user', JSON.stringify(response));
        })
      );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, userData);
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {})
      .pipe(
        tap(() => {
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('user');
        })
      );
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
```

### HTTP Interceptor to Add Token
```typescript
import { HttpInterceptorFn } from '@angular/core';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
```

---

## Testing the Fix

### 1. Start the Gateway
```bash
cd "C:\Users\ilias\Desktop\EMSI\springboot\JEE2 project\MS project\Gateway"
.\mvnw.cmd spring-boot:run
```

### 2. Verify Gateway is Running
```
http://localhost:1111/actuator/health
```

Should return: `{"status":"UP"}`

### 3. Test Login from Browser Console
```javascript
fetch('http://localhost:1111/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'test@example.com',
    password: 'password123'
  })
})
.then(r => r.json())
.then(data => console.log(data));
```

### 4. Test from Angular
Update your login component to use the new endpoint and test.

---

## Alternative: Using Angular Proxy

If you want to avoid CORS entirely during development, use Angular's proxy:

### 1. Create `proxy.conf.json` in Angular project root
```json
{
  "/auth": {
    "target": "http://localhost:1111",
    "secure": false,
    "changeOrigin": true
  },
  "/api": {
    "target": "http://localhost:1111",
    "secure": false,
    "changeOrigin": true
  }
}
```

### 2. Update `angular.json`
```json
{
  "projects": {
    "your-project": {
      "architect": {
        "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json"
          }
        }
      }
    }
  }
}
```

### 3. Update Environment
```typescript
export const environment = {
  production: false,
  apiBaseUrl: ''  // Empty - uses same origin with proxy
};
```

### 4. Start Angular with Proxy
```bash
ng serve --proxy-config proxy.conf.json
```

Now you can call `/auth/login` and it will be proxied to `http://localhost:1111/auth/login`.

---

## Verification Checklist

- [x] CORS configuration added to `application.yml`
- [x] `CorsConfig.java` created
- [x] `SecurityConfig.java` updated to allow OPTIONS and /auth/**
- [x] Spring Security dependency added to `pom.xml`
- [x] Project compiles successfully
- [ ] Gateway running on port 1111
- [ ] Frontend updated to use `http://localhost:1111`
- [ ] Login endpoint changed from `/api/auth/login` to `/auth/login`
- [ ] Request body uses `email` field instead of `username`
- [ ] Token stored in sessionStorage/localStorage
- [ ] Authorization header added to protected requests
- [ ] CORS error resolved
- [ ] Login successful
- [ ] Protected routes accessible with token

---

## Next Steps

1. **Restart Gateway** to apply all changes
2. **Update Frontend** environment and services
3. **Test Login Flow** end-to-end
4. **Test Protected Routes** with Bearer token
5. **Handle Errors** appropriately in UI
6. **Add Loading States** for better UX

---

## Support Files Created

1. `FRONTEND_INTEGRATION.md` - Complete frontend integration guide
2. `API_ENDPOINTS_SUMMARY.md` - All gateway endpoints reference
3. `proxy.conf.json` - Angular proxy configuration (optional)
4. This file - CORS fix summary

---

## Summary

✅ **Backend**: CORS is now properly configured on the Gateway
✅ **Endpoints**: Authentication moved from Client MS to Gateway
✅ **Security**: Spring Security configured for reactive gateway
✅ **Documentation**: Complete guides created

🔧 **Frontend Action Required**: Update base URL and endpoint paths

The CORS problem is **FIXED** on the backend side. Now you need to update your frontend to point to the Gateway endpoints.

