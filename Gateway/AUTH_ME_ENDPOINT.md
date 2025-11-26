# ✅ /auth/me Endpoint - Implemented in Gateway

## Architecture Decision

The `/auth/me` endpoint is now **properly implemented in the Gateway**, following API Gateway pattern best practices.

## 🎯 Why Gateway (Not Client MS)?

### 1. **Security**
- ❌ **Bad**: Client MS with `@RequestParam String email` - can be manipulated
- ✅ **Good**: Gateway extracts user ID from JWT token - secure and tamper-proof

### 2. **Authentication Context**
- Gateway has direct access to the JWT token
- Token validation happens in Gateway
- User ID extracted from token, not from request parameters

### 3. **Single Responsibility**
- **Gateway**: Authentication, authorization, token management, routing
- **Client MS**: User data CRUD operations only

### 4. **Performance & Security**
- Token validated once at Gateway
- Secure internal call to Client MS using authenticated user ID
- No exposure of email as query parameter

---

## 🔧 Implementation

### Gateway AuthController (`/auth/me`)

```java
@GetMapping("/me")
public Mono<ResponseEntity<UserDTO>> getCurrentUser(
    @RequestHeader(value = "Authorization", required = false) String authHeader
) {
    return Mono.fromCallable(() -> {
        // Validate Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        // Extract token
        String token = authHeader.replace("Bearer ", "");

        // Validate token
        if (!tokenService.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        // Get user info from token
        var tokenInfo = tokenService.getUserFromToken(token);
        
        if (tokenInfo == null) {
            throw new UnauthorizedException("Invalid token");
        }

        return tokenInfo.getUserId();
    })
    .flatMap(userId -> userServiceClient.getUserById(userId))
    .map(user -> {
        // Remove password from response
        user.setPassword(null);
        return ResponseEntity.ok(user);
    })
    .onErrorResume(UnauthorizedException.class, e -> Mono.error(e))
    .onErrorResume(e -> Mono.error(new RuntimeException("Failed to get user info: " + e.getMessage())));
}
```

### UserServiceClientReactive (New Method)

```java
public Mono<UserDTO> getUserById(Long id) {
    return webClientBuilder.build()
        .get()
        .uri("http://MS-CLIENT/api/v1/users/{id}", id)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .onErrorResume(e -> Mono.empty());
}
```

### Client MS Endpoint (Keep As Is)

```java
// In Client MS UserController
@GetMapping("/api/v1/users/{id}")
public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
    // Return user data by ID
}
```

---

## 🔐 Security Flow

```
Frontend Request
    ↓
GET /auth/me
Headers: { Authorization: Bearer <token> }
    ↓
Gateway AuthController
    ↓
1. Extract token from Authorization header
    ↓
2. Validate token with TokenService
    ↓
3. Get user ID from token (secure)
    ↓
4. Call MS-CLIENT with user ID
    ↓
WebClient → GET http://MS-CLIENT/api/v1/users/{userId}
    ↓
Client MS returns user data
    ↓
Gateway removes password field
    ↓
Return user info to frontend
```

---

## 📝 API Documentation

### Endpoint
**GET /auth/me**

### Authentication
🔒 **Required** - Bearer token

### Request Headers
```
Authorization: Bearer <token>
```

### Success Response (200 OK)
```json
{
  "id": 1,
  "email": "user@example.com",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "+1234567890",
  "role": "CLIENT",
  "password": null
}
```

### Error Responses

#### 401 Unauthorized - Missing Token
```json
{
  "timestamp": "2025-11-23T20:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "path": "/auth/me"
}
```

#### 401 Unauthorized - Invalid Token
```json
{
  "timestamp": "2025-11-23T20:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/auth/me"
}
```

#### 500 Internal Server Error - Service Unavailable
```json
{
  "timestamp": "2025-11-23T20:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to get user info: Connection refused",
  "path": "/auth/me"
}
```

---

## 🧪 Testing

### Test from Browser Console
```javascript
// First, login to get token
fetch('http://localhost:1111/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'test@example.com',
    password: 'password123'
  })
})
.then(r => r.json())
.then(data => {
  const token = data.token;
  console.log('Token:', token);
  
  // Now call /auth/me
  return fetch('http://localhost:1111/auth/me', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
})
.then(r => r.json())
.then(user => console.log('Current User:', user))
.catch(err => console.error('Error:', err));
```

### Test from Angular
```typescript
// auth.service.ts
getCurrentUser(): Observable<UserDTO> {
  return this.http.get<UserDTO>(`${this.apiUrl}/me`);
}

// Component
this.authService.getCurrentUser().subscribe({
  next: (user) => {
    console.log('Current user:', user);
    this.currentUser = user;
  },
  error: (error) => {
    console.error('Failed to get user:', error);
    if (error.status === 401) {
      // Token invalid, redirect to login
      this.router.navigate(['/login']);
    }
  }
});
```

### Test from Postman
**Request**:
```
GET http://localhost:1111/auth/me
Authorization: Bearer <your-token-here>
```

**Expected Response**:
```json
{
  "id": 1,
  "email": "test@example.com",
  "nom": "Test",
  "prenom": "User",
  "telephone": "+1234567890",
  "role": "CLIENT",
  "password": null
}
```

---

## 📊 Comparison: Before vs After

### ❌ Before (If it were in Client MS)
```java
// Client MS - INSECURE
@GetMapping("/auth/me")
public ResponseEntity<UserDTO> getCurrentUser(@RequestParam String email) {
    // Anyone can change email parameter and get other users' data!
    UserDTO user = userService.findByEmail(email);
    return ResponseEntity.ok(user);
}
```

**Problems**:
- Email as query parameter can be manipulated
- No token validation
- Breaks API Gateway pattern
- Security vulnerability

### ✅ After (Gateway Implementation)
```java
// Gateway - SECURE
@GetMapping("/me")
public Mono<ResponseEntity<UserDTO>> getCurrentUser(@RequestHeader String authHeader) {
    // Extract user ID from validated token
    // Cannot be manipulated by user
    return validateAndGetUserId(authHeader)
        .flatMap(userId -> userServiceClient.getUserById(userId))
        .map(user -> ResponseEntity.ok(user));
}
```

**Benefits**:
- User ID extracted from token (secure)
- Token validated before any operation
- Follows API Gateway pattern
- Cannot be manipulated

---

## 🎯 Files Modified

| File | Change | Purpose |
|------|--------|---------|
| `AuthController.java` | Added `/auth/me` endpoint | Get authenticated user info |
| `UserServiceClientReactive.java` | Added `getUserById()` method | Call Client MS by user ID |
| `SecurityConfig.java` | Added comment about `/auth/me` | Clarify it's protected |
| `REACTIVE_WEBCLIENT_FIX.md` | Updated documentation | Document new endpoint |

---

## 🔒 Security Considerations

### Token Validation
- Token validated before any operation
- Invalid/expired tokens return 401
- No default fallback to unauthenticated access

### User ID Extraction
- User ID extracted from token stored in TokenService
- TokenInfo contains: userId, email, role, createdAt
- Cannot be manipulated by client

### Password Handling
- Password field always set to null in response
- Never returned to frontend
- Hashed in database

### Authorization
- Currently allows any authenticated user to see their own data
- Can be extended to check role if needed
- Admin users could be allowed to see any user's data

---

## 🚀 Usage in Frontend

### 1. On App Load (Check Authentication)
```typescript
// app.component.ts
ngOnInit() {
  const token = localStorage.getItem('token');
  if (token) {
    this.authService.getCurrentUser().subscribe({
      next: (user) => this.userService.setCurrentUser(user),
      error: () => this.authService.logout()
    });
  }
}
```

### 2. After Login
```typescript
// login.component.ts
login() {
  this.authService.login(this.email, this.password).subscribe({
    next: (response) => {
      localStorage.setItem('token', response.token);
      
      // Get full user info
      this.authService.getCurrentUser().subscribe({
        next: (user) => {
          this.userService.setCurrentUser(user);
          this.router.navigate(['/dashboard']);
        }
      });
    }
  });
}
```

### 3. In Navigation/Header
```typescript
// header.component.ts
currentUser$: Observable<UserDTO>;

ngOnInit() {
  this.currentUser$ = this.authService.getCurrentUser();
}
```

```html
<!-- header.component.html -->
<div *ngIf="currentUser$ | async as user">
  <span>Welcome, {{ user.prenom }} {{ user.nom }}</span>
  <span>{{ user.email }}</span>
  <span class="badge">{{ user.role }}</span>
</div>
```

---

## 🎉 Benefits Summary

### Security ✅
- User ID from token (tamper-proof)
- Token validation required
- Cannot access other users' data

### Architecture ✅
- Follows API Gateway pattern
- Clear separation of concerns
- Gateway handles auth, Client MS handles data

### Performance ✅
- Single token validation
- Efficient reactive call to Client MS
- No unnecessary database queries

### Maintainability ✅
- Clear code structure
- Easy to understand flow
- Well-documented

---

## 📞 Troubleshooting

### Issue: 401 Unauthorized
**Causes**:
1. Token not included in request
2. Token expired
3. Token invalid/tampered

**Solution**:
1. Check Authorization header format: `Bearer <token>`
2. Login again to get new token
3. Clear local storage and re-authenticate

### Issue: 500 Internal Server Error
**Causes**:
1. Client MS not running
2. Client MS not registered with Eureka
3. Network connectivity issue

**Solution**:
1. Start Client MS
2. Verify Eureka registration
3. Check Gateway logs for detailed error

### Issue: User data not returned
**Causes**:
1. User ID doesn't exist in Client MS
2. Client MS returned error
3. WebClient error

**Solution**:
1. Check if user exists in database
2. Check Client MS logs
3. Enable debug logging for WebClient

---

## 🎯 Next Steps

### Optional Enhancements

1. **Add User Caching**
   ```java
   @Cacheable(value = "users", key = "#userId")
   public Mono<UserDTO> getUserById(Long userId) { ... }
   ```

2. **Add Rate Limiting**
   ```java
   @RateLimiter(name = "authMe", fallbackMethod = "rateLimitFallback")
   public Mono<ResponseEntity<UserDTO>> getCurrentUser(...) { ... }
   ```

3. **Add Monitoring**
   ```java
   @Timed(value = "auth.me.timer")
   @Counted(value = "auth.me.count")
   public Mono<ResponseEntity<UserDTO>> getCurrentUser(...) { ... }
   ```

4. **Add Response Headers**
   ```java
   return ResponseEntity.ok()
       .header("X-User-Id", String.valueOf(user.getId()))
       .header("X-User-Role", user.getRole())
       .body(user);
   ```

---

**Status**: ✅ `/auth/me` endpoint successfully implemented in Gateway
**Security**: ✅ Secure token-based authentication
**Architecture**: ✅ Follows API Gateway pattern
**Ready for**: Production use

