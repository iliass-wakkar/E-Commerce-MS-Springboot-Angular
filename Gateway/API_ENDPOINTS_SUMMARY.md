# Gateway Microservice - API Endpoints Summary

## Overview
This document provides a complete reference of all endpoints exposed by the Gateway microservice after authentication was migrated from the Client microservice.

**Base URL**: `http://localhost:1111`

---

## Authentication Endpoints

### POST /auth/login
Authenticate user and receive access token

**Request**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200):
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "email": "user@example.com",
  "role": "CLIENT"
}
```

**Errors**:
- 401: Invalid email or password
- 400: Missing required fields

---

### POST /auth/register
Create new user account

**Request**:
```json
{
  "email": "newuser@example.com",
  "password": "securePass123",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "+1234567890",
  "role": "CLIENT"
}
```

**Response** (201):
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

**Errors**:
- 400: Email already exists or validation error
- 500: Registration failed

---

### POST /auth/logout
Invalidate user token

**Headers**:
```
Authorization: Bearer <token>
```

**Response** (200):
```json
{
  "message": "Logged out successfully"
}
```

**Errors**:
- 401: Invalid or missing token

---

## Gateway Routes Configuration

The gateway forwards requests to downstream microservices based on path patterns.

### Typical Route Patterns

#### Client Service Routes
- `GET /api/clients` - Get all clients (ADMIN)
- `GET /api/clients/{id}` - Get client by ID
- `POST /api/clients` - Create client
- `PUT /api/clients/{id}` - Update client
- `DELETE /api/clients/{id}` - Delete client (ADMIN)

#### Product Service Routes (if exists)
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create product (ADMIN)
- `PUT /api/products/{id}` - Update product (ADMIN)
- `DELETE /api/products/{id}` - Delete product (ADMIN)

#### Order Service Routes (if exists)
- `GET /api/orders` - Get user orders
- `GET /api/orders/{id}` - Get order details
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}` - Update order status (ADMIN)

---

## Request/Response Flow

### 1. Login Flow
```
Frontend → POST /auth/login → Gateway (AuthController)
                               ↓
                        UserServiceClient (Feign)
                               ↓
                        Client Microservice
                               ↓
                        Validate credentials
                               ↓
                        Generate token (TokenService)
                               ↓
                        Return token + user info
```

### 2. Protected Resource Flow
```
Frontend → GET /api/resource → Gateway
            (with Bearer token)     ↓
                            AuthenticationFilter
                                    ↓
                            Validate token
                                    ↓
                            Forward to microservice
                                    ↓
                            Return response
```

---

## Authentication & Authorization

### Token-Based Authentication
- Token generated on successful login
- Token stored in memory (TokenService)
- Token TTL: 86400 seconds (24 hours)
- Token format: UUID-like string

### Authorization Levels
1. **PUBLIC**: No authentication required
   - `/auth/login`
   - `/auth/register`

2. **AUTHENTICATED**: Valid token required
   - Most `/api/**` endpoints

3. **ADMIN**: Admin role required
   - Delete operations
   - User management
   - System configuration

---

## Filters Applied

### 1. AuthenticationFilter
- Path: `/api/**`
- Validates Bearer token
- Extracts user info from token
- Adds user context to request

### 2. AdminAuthorizationFilter
- Path: Admin-specific routes
- Checks user role = "ADMIN"
- Returns 403 if not admin

### 3. CustomGatewayFilter
- Global logging
- Request/response monitoring
- Performance tracking

### 4. CorsWebFilter
- Allows origin: `http://localhost:4200`
- Allows all methods and headers
- Exposes Authorization header
- Credentials: true

---

## Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/auth/login"
}
```

### Custom Exception Handling

#### UnauthorizedException (401)
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/auth/login"
}
```

#### ForbiddenException (403)
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required.",
  "path": "/api/admin/users"
}
```

---

## Service Integration

### UserServiceClient (Feign)
Communicates with Client microservice for user operations

**Methods**:
- `getUserByEmail(email)` - Used in login
- `createUser(userDTO)` - Used in registration
- `getUserById(id)` - Used for token validation

**Configuration**:
- Circuit breaker enabled
- Retry on failure
- Timeout: 5000ms
- Logger level: FULL

---

## Security Configuration

### CORS
```yaml
allowedOrigins: "http://localhost:4200"
allowedMethods: "*"
allowedHeaders: "*"
exposedHeaders: "Authorization"
allowCredentials: true
```

### Security Rules
```java
.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
.pathMatchers("/auth/**").permitAll()
.anyExchange().authenticated()
```

### Password Encryption
- Algorithm: BCrypt
- Strength: Default (10 rounds)
- Handled by: BCryptPasswordEncoder

---

## Token Management

### TokenService Methods
- `generateToken()` - Creates new UUID token
- `storeToken(token, userId, email, role)` - Stores in memory
- `validateToken(token)` - Checks if valid
- `getTokenInfo(token)` - Returns user info
- `invalidateToken(token)` - Removes from storage

### Token Storage
- In-memory Map (ConcurrentHashMap)
- No persistence (tokens lost on restart)
- **Recommendation**: Implement Redis for production

---

## Frontend Integration Requirements

### 1. Update Base URL
```typescript
environment.apiBaseUrl = 'http://localhost:1111'
```

### 2. Login Request
```typescript
// OLD (Client MS)
POST http://localhost:8081/api/auth/login

// NEW (Gateway)
POST http://localhost:1111/auth/login
```

### 3. Include Token in Requests
```typescript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

### 4. Handle CORS
No special handling needed if using `http://localhost:4200`

### 5. Error Handling
Update to handle new error format from gateway

---

## Configuration Files

### application.yml
```yaml
spring:
  application:
    name: reactivegateway
  cloud:
    gateway:
      globalcors:
        # CORS configuration
        
server:
  port: 1111

gateway:
  auth:
    token:
      ttl-seconds: 86400
```

---

## Monitoring & Health

### Actuator Endpoints
- `/actuator/health` - Gateway health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Performance metrics

---

## Migration Checklist

- [x] Move AuthController to Gateway
- [x] Implement TokenService in Gateway
- [x] Configure CORS for frontend
- [x] Add Spring Security dependencies
- [x] Create authentication/authorization filters
- [x] Setup Feign client for User service
- [x] Configure exception handling
- [x] Add security configuration
- [ ] Update frontend to use new endpoints
- [ ] Test all authentication flows
- [ ] Update API documentation
- [ ] Configure Redis for token storage (production)
- [ ] Add refresh token mechanism
- [ ] Implement rate limiting
- [ ] Add request/response logging
- [ ] Configure SSL/TLS for production

---

## Testing

### Manual Testing
```bash
# Login
curl -X POST http://localhost:1111/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Access Protected Resource
curl -X GET http://localhost:1111/api/products \
  -H "Authorization: Bearer <token>"

# Logout
curl -X POST http://localhost:1111/auth/logout \
  -H "Authorization: Bearer <token>"
```

---

## Known Issues & Limitations

1. **Token Storage**: Currently in-memory, will be lost on restart
   - **Solution**: Implement Redis or database persistence

2. **No Refresh Token**: Users must re-login after token expires
   - **Solution**: Implement refresh token mechanism

3. **No Rate Limiting**: Vulnerable to brute force attacks
   - **Solution**: Add Spring Cloud Gateway rate limiter

4. **No Request Validation**: Limited input validation
   - **Solution**: Add Bean Validation annotations

---

## Future Enhancements

- [ ] JWT-based tokens instead of UUID
- [ ] Refresh token mechanism
- [ ] OAuth2/OpenID Connect support
- [ ] Multi-factor authentication
- [ ] Redis token storage
- [ ] Rate limiting per user
- [ ] API versioning
- [ ] Request/response logging to file
- [ ] Distributed tracing (Zipkin/Jaeger)
- [ ] API gateway metrics dashboard

---

## Contact & Support

**Gateway Port**: 1111  
**Health Check**: http://localhost:1111/actuator/health  
**Log Level**: DEBUG (development)  

For issues or questions, contact the backend development team.

