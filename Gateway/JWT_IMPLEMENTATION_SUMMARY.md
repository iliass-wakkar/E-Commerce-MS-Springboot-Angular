# JWT Authentication Implementation Summary

## Overview
Successfully upgraded the Gateway microservice from UUID-based tokens to industry-standard **JWT (JSON Web Tokens)** with Spring Security integration.

## What Was Implemented

### 1. **Dependencies Added** (pom.xml)
- `io.jsonwebtoken:jjwt-api:0.12.3` - JWT API
- `io.jsonwebtoken:jjwt-impl:0.12.3` - JWT implementation
- `io.jsonwebtoken:jjwt-jackson:0.12.3` - JSON serialization

### 2. **New Components Created**

#### JwtTokenProvider.java
- **Location**: `src/main/java/com/Gateway/Server/service/JwtTokenProvider.java`
- **Purpose**: Generate and validate JWT tokens with proper signing
- **Features**:
  - Creates JWT tokens with claims (userId, email, role)
  - Signs tokens with HS256 algorithm using secret key
  - Validates token signature and expiration
  - Extracts user information from JWT claims
  - Handles all JWT-related exceptions

#### JwtAuthenticationFilter.java
- **Location**: `src/main/java/com/Gateway/Server/filters/JwtAuthenticationFilter.java`
- **Purpose**: Reactive WebFilter for JWT validation
- **Features**:
  - Extracts JWT from `Authorization: Bearer <token>` header
  - Validates JWT signature and expiration
  - Populates request attributes with user info
  - Adds custom headers (X-User-Id, X-User-Email, X-User-Role) for downstream services
  - Skips validation for public endpoints

#### JwtException.java
- **Location**: `src/main/java/com/Gateway/Server/exception/JwtException.java`
- **Purpose**: Custom exception for JWT-related errors
- **Handled By**: GlobalExceptionHandler (returns 401 Unauthorized)

### 3. **Updated Components**

#### TokenService.java
- **Before**: Generated UUID tokens, stored in memory
- **After**: 
  - Generates JWT tokens using JwtTokenProvider
  - Validates JWT tokens (signature + expiration)
  - Supports token blacklist for logout functionality
  - Extracts user info from JWT claims

#### AuthController.java
- **Updated**: `/auth/login` endpoint now:
  - Generates JWT token instead of UUID
  - Includes `expiresIn` field in response (token TTL in seconds)
  - Returns proper JWT to frontend

#### LoginResponse.java
- **Added**: `expiresIn` field to inform frontend about token expiration time

#### SecurityConfig.java
- **Enhanced**:
  - Properly integrated with reactive Spring Security
  - Public endpoints: `/auth/login`, `/auth/register`
  - Protected endpoints: `/auth/me`, `/auth/logout` (require JWT)
  - Disabled CSRF, form login, HTTP Basic (using JWT only)

#### GlobalExceptionHandler.java
- **Added**: Handler for `JwtException` → returns 401 with proper error message

#### TokenInfo.java
- **Added**: `blacklisted` flag for logout/token revocation

#### application.yml
- **Added JWT Configuration**:
  ```yaml
  gateway:
    jwt:
      secret-key: your-super-secret-key-for-jwt-token-generation-min-256-bits-change-me
      ttl-seconds: 86400  # 24 hours
      issuer: gateway-server
  ```

## JWT Token Structure

### Claims Included:
```json
{
  "sub": "123",           // Subject (userId)
  "email": "user@example.com",
  "role": "CLIENT",       // or "ADMIN"
  "userId": 123,
  "iss": "gateway-server", // Issuer
  "iat": 1702204800,      // Issued At timestamp
  "exp": 1702291200       // Expiration timestamp
}
```

### Token Format:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjoiQ0xJRU5UIiwidXNlcklkIjoxMjMsImlzcyI6ImdhdGV3YXktc2VydmVyIiwiaWF0IjoxNzAyMjA0ODAwLCJleHAiOjE3MDIyOTEyMDB9.signature
```

## Security Features

### ✅ Implemented:
1. **Token Signing**: HMAC-SHA256 signature prevents tampering
2. **Token Expiration**: Automatic expiration after 24 hours
3. **Signature Validation**: Rejects modified or forged tokens
4. **Expiration Validation**: Rejects expired tokens
5. **Token Blacklist**: Logout invalidates tokens (in-memory for academic project)
6. **Exception Handling**: Proper error messages for invalid/expired tokens
7. **Spring Security Integration**: Reactive security with JWT filters

### 🔒 Security Best Practices:
- Secret key stored in configuration (can be externalized to environment variables)
- HS256 algorithm (symmetric signing - suitable for single gateway)
- Claims include role-based access control (RBAC)
- No sensitive data in JWT payload (it's base64 encoded, not encrypted)

## API Endpoints

### Public Endpoints (No JWT Required):
- `POST /auth/login` - Login and receive JWT token
- `POST /auth/register` - Register new user

### Protected Endpoints (JWT Required):
- `GET /auth/me` - Get current user info
- `POST /auth/logout` - Invalidate JWT token
- All gateway routes to microservices

## How It Works

### 1. Login Flow:
```
1. Frontend sends POST /auth/login with {email, password}
2. Gateway validates credentials via User Service
3. Gateway generates JWT token with user claims
4. Gateway returns {token, userId, email, role, expiresIn}
5. Frontend stores token (localStorage/sessionStorage)
```

### 2. Authenticated Request Flow:
```
1. Frontend sends request with: Authorization: Bearer <JWT>
2. JwtAuthenticationFilter intercepts request
3. Filter validates JWT signature and expiration
4. Filter extracts claims and adds to request headers
5. Gateway routes request to microservice with headers:
   - X-User-Id: 123
   - X-User-Email: user@example.com
   - X-User-Role: CLIENT
6. Microservice processes request with user context
```

### 3. Logout Flow:
```
1. Frontend sends POST /auth/logout with JWT
2. Gateway blacklists the token (sets blacklisted=true)
3. Token becomes invalid even if not expired
4. Frontend removes token from storage
```

## Frontend Integration

### Login Request:
```typescript
login(email: string, password: string) {
  return this.http.post('http://localhost:1111/auth/login', {
    email: email,
    password: password
  });
}
```

### Login Response:
```json
{
  "token": "eyJhbGc...",
  "userId": 123,
  "email": "user@example.com",
  "role": "CLIENT",
  "expiresIn": 86400
}
```

### Authenticated Requests:
```typescript
// Add token to all requests
const headers = new HttpHeaders({
  'Authorization': `Bearer ${token}`
});

this.http.get('http://localhost:1111/PRODUCT-SERVICE/products', { headers });
```

## Configuration

### application.yml Settings:
```yaml
gateway:
  jwt:
    secret-key: <your-secret-key>  # Change in production!
    ttl-seconds: 86400             # Token lifetime (24 hours)
    issuer: gateway-server         # Token issuer name
```

### Security Recommendations:
1. **Change the secret key** to a random 256-bit string
2. For production: Use environment variables for secret key
3. Consider shorter TTL (1-4 hours) for better security
4. Implement refresh tokens for seamless user experience
5. Add rate limiting on login endpoint

## Testing

### Test Login:
```bash
curl -X POST http://localhost:1111/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Test Protected Endpoint:
```bash
curl http://localhost:1111/auth/me \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Test Invalid Token:
```bash
# Should return 401 Unauthorized
curl http://localhost:1111/auth/me \
  -H "Authorization: Bearer invalid-token"
```

## Benefits Over UUID Tokens

| Feature | UUID Tokens | JWT Tokens |
|---------|-------------|------------|
| **Signature** | ❌ None | ✅ HMAC-SHA256 |
| **Expiration** | ❌ Manual | ✅ Built-in |
| **Stateless** | ❌ Requires storage | ✅ Self-contained |
| **Claims** | ❌ None | ✅ User info embedded |
| **Standard** | ❌ Custom | ✅ Industry standard (RFC 7519) |
| **Validation** | ❌ DB lookup | ✅ Signature verification |
| **Tamper-proof** | ❌ Easy to forge | ✅ Cryptographically signed |

## Next Steps (Optional Enhancements)

1. **Refresh Tokens**: Implement `/auth/refresh` endpoint for token renewal
2. **Redis Integration**: Replace in-memory storage with Redis for production
3. **Role-Based Access**: Add `@PreAuthorize` annotations for endpoint security
4. **Token Rotation**: Rotate tokens on refresh for better security
5. **Audit Logging**: Log all authentication events
6. **Rate Limiting**: Prevent brute-force attacks on login
7. **Multi-factor Authentication**: Add 2FA support

## Troubleshooting

### Common Issues:

1. **"Invalid JWT signature"**
   - Check that secret key matches between token generation and validation
   - Ensure secret key is at least 256 bits (64 characters)

2. **"JWT token has expired"**
   - Token TTL exceeded (default 24 hours)
   - Frontend should handle by redirecting to login

3. **"No Authorization header"**
   - Frontend not sending `Authorization: Bearer <token>`
   - Check CORS configuration allows Authorization header

4. **401 on /auth/login**
   - Invalid credentials
   - User service unreachable
   - Password not BCrypt-hashed in database

## Summary

✅ **JWT authentication is now fully implemented and integrated with Spring Security!**

Your Gateway microservice now uses industry-standard JWT tokens with:
- Cryptographic signing (HS256)
- Automatic expiration validation
- Role-based access control
- Proper exception handling
- Reactive Spring Security integration
- No external dependencies (Redis) for academic simplicity

The implementation follows Spring Security best practices and is ready for frontend integration.

