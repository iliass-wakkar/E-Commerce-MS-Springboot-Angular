# API Gateway Authentication System

## Overview
This Gateway implements token-based authentication with role-based access control (RBAC). All incoming requests are validated at the Gateway level before being forwarded to microservices.

## Architecture

### Components
1. **AuthController** - Handles login, logout, and registration
2. **TokenService** - Manages in-memory token storage and validation
3. **AuthenticationFilter** - Validates tokens and adds user context to requests
4. **AdminAuthorizationFilter** - Enforces admin-only access to specific routes
5. **UserServiceClient** - Feign client to communicate with Client-MS

### Authentication Flow

#### 1. User Registration
```
Browser -> POST /auth/register
-> Gateway AuthController
-> Password hashed with BCrypt
-> Gateway calls Client-MS to create user
-> Returns user info (without password)
```

#### 2. User Login
```
Browser -> POST /auth/login
-> Gateway AuthController
-> Gateway calls Client-MS to get user by email
-> Gateway validates password (BCrypt)
-> Gateway generates UUID token
-> Gateway stores token in memory
-> Returns { token, userId, email, role }
```

#### 3. Authenticated Request
```
Browser -> GET /MS-CLIENT/users/123
         (with Authorization: Bearer <token>)
-> Gateway AuthenticationFilter extracts token
-> Gateway validates token
-> Gateway adds headers:
   - X-User-Id
   - X-User-Email
   - X-User-Role
-> Gateway forwards to Client-MS
-> Client-MS receives request with user context
```

#### 4. Admin Request
```
Browser -> GET /MS-CLIENT/users
         (with Authorization: Bearer <token>)
-> Gateway AuthenticationFilter validates token
-> Gateway AdminAuthorizationFilter checks role
-> If ADMIN: forward to Client-MS
-> If not ADMIN: return 403 Forbidden
```

#### 5. Logout
```
Browser -> POST /auth/logout
         (with Authorization: Bearer <token>)
-> Gateway removes token from memory
-> Returns 200 OK
```

## API Endpoints

### Authentication Endpoints (Public)

#### POST /auth/register
Register a new user.

**Request:**
```json
{
  "nom": "John",
  "prenom": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "telephone": "0612345678",
  "role": "CLIENT"
}
```

**Response:**
```json
{
  "id": 1,
  "nom": "John",
  "prenom": "Doe",
  "email": "john.doe@example.com",
  "telephone": "0612345678",
  "role": "CLIENT"
}
```

#### POST /auth/login
Authenticate a user and receive a token.

**Request:**
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "email": "john.doe@example.com",
  "role": "CLIENT"
}
```

#### POST /auth/logout
Invalidate the current token.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "Logged out successfully"
}
```

### Gateway Routes

#### Public Routes (No Authentication Required)
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `GET /product-service/**` - Browse products (read-only)
- `GET /MS-CLIENT/users/email/{email}` - Get user by email (used internally)

#### Protected Routes (Authentication Required)
- `POST /product-service/**` - Create products (requires valid token)
- `PUT /product-service/**` - Update products (requires valid token)
- `DELETE /product-service/**` - Delete products (requires valid token)
- `GET /MS-CLIENT/users/{id}` - Get user by ID (requires valid token)

#### Admin Routes (Admin Role Required)
- `GET /MS-CLIENT/users` - Get all users (admin only)
- `PUT /MS-CLIENT/users/{id}` - Update user (admin only)
- `DELETE /MS-CLIENT/users/{id}` - Delete user (admin only)

## Request Headers

### Sent by Client
```
Authorization: Bearer <token>
```

### Added by Gateway (to downstream services)
```
X-User-Id: 1
X-User-Email: john.doe@example.com
X-User-Role: CLIENT
X-Request-Origin: Gateway
```

## Configuration

### application.yml
```yaml
spring:
  application:
    name: reactivegateway
  config:
    import: optional:configserver:http://localhost:8888

server:
  port: 1111

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    feign: DEBUG

feign:
  client:
    config:
      default:
        loggerLevel: full
        connectTimeout: 5000
        readTimeout: 5000

gateway:
  auth:
    token:
      ttl-seconds: 86400  # 1 day (currently not enforced, can be implemented)
```

## Security Features

### 1. Password Hashing
- Uses BCrypt for password hashing
- Passwords are never stored or transmitted in plain text
- Configured with default strength (10 rounds)

### 2. Token-Based Authentication
- UUID tokens for stateless authentication
- Tokens stored in-memory (ConcurrentHashMap for thread-safety)
- Tokens can be invalidated via logout

### 3. Role-Based Access Control
- Two roles: CLIENT and ADMIN
- Admin routes protected by AdminAuthorizationFilter
- Role information forwarded to microservices via headers

### 4. Request Filtering
- All protected routes pass through AuthenticationFilter
- Admin routes additionally pass through AdminAuthorizationFilter
- Invalid tokens result in 401 Unauthorized
- Insufficient permissions result in 403 Forbidden

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-19T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-19T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required."
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-11-19T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error details..."
}
```

## Testing the Authentication System

### 1. Register a User
```bash
curl -X POST http://localhost:1111/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "John",
    "prenom": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "telephone": "0612345678",
    "role": "CLIENT"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:1111/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### 3. Access Protected Resource
```bash
curl -X GET http://localhost:1111/MS-CLIENT/users/1 \
  -H "Authorization: Bearer <your-token-here>"
```

### 4. Access Admin Resource (will fail if not admin)
```bash
curl -X GET http://localhost:1111/MS-CLIENT/users \
  -H "Authorization: Bearer <your-token-here>"
```

### 5. Logout
```bash
curl -X POST http://localhost:1111/auth/logout \
  -H "Authorization: Bearer <your-token-here>"
```

## Dependencies Added

```xml
<!-- Spring Boot Starter WebFlux for REST controllers -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- OpenFeign for calling Client-MS -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Spring Cloud LoadBalancer for Feign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- BCrypt for password hashing -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>

<!-- Jackson for JSON processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

## Future Enhancements

1. **Token Expiration** - Implement TTL for tokens using the configured `gateway.auth.token.ttl-seconds`
2. **Refresh Tokens** - Add refresh token mechanism for long-lived sessions
3. **Database Token Storage** - Replace in-memory storage with Redis or database
4. **Rate Limiting** - Add rate limiting to prevent brute force attacks
5. **JWT Tokens** - Replace UUID tokens with JWT for stateless validation
6. **OAuth2/OpenID Connect** - Integrate with OAuth2 providers
7. **Audit Logging** - Log all authentication and authorization events

## Notes

- Token storage is in-memory and will be lost on Gateway restart
- Each microservice receives user context via headers (X-User-Id, X-User-Email, X-User-Role)
- Microservices can trust these headers as they come from the Gateway
- For production, consider implementing proper token expiration and refresh mechanisms

