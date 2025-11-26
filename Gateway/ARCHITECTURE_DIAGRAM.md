# Gateway Authentication Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT (Browser/App)                       │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ HTTP Requests
                             │ (with/without Authorization header)
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Port 1111)                         │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │                      Route Matching                              │ │
│ │  /auth/**           → AuthController (local)                     │ │
│ │  /product-service/** → product-service (via Eureka)              │ │
│ │  /MS-CLIENT/**      → MS-CLIENT (via Eureka)                     │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                             ▼                                        │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │                    Filter Chain                                  │ │
│ │                                                                  │ │
│ │  1. MyGlobalLogFilter (logs all requests)                       │ │
│ │  2. AuthenticationFilter (validates token)                       │ │
│ │  3. AdminAuthorizationFilter (checks admin role)                │ │
│ │  4. CustomGatewayFilter (custom logic)                          │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                             ▼                                        │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │              Add User Context Headers                            │ │
│ │              X-User-Id: 123                                      │ │
│ │              X-User-Email: user@example.com                      │ │
│ │              X-User-Role: CLIENT                                 │ │
│ └─────────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
         ┌──────────────────┐  ┌──────────────────┐
         │  Client-MS       │  │  Product-Service │
         │  (MS-CLIENT)     │  │                  │
         │  Port: 8081      │  │  Port: 8082      │
         └──────────────────┘  └──────────────────┘
                    │
                    │ (registered via)
                    ▼
         ┌──────────────────┐
         │  Eureka Server   │
         │  Port: 8761      │
         └──────────────────┘
```

## Authentication Flow Diagram

### 1. Registration Flow
```
┌────────┐                  ┌─────────┐                 ┌───────────┐
│ Client │                  │ Gateway │                 │ Client-MS │
└───┬────┘                  └────┬────┘                 └─────┬─────┘
    │                            │                            │
    │ POST /auth/register        │                            │
    │ {email, password, ...}     │                            │
    ├───────────────────────────►│                            │
    │                            │                            │
    │                            │ 1. Hash password (BCrypt)  │
    │                            │                            │
    │                            │ 2. POST /api/v1/users      │
    │                            │    (via Feign)             │
    │                            ├───────────────────────────►│
    │                            │                            │
    │                            │ 3. User created            │
    │                            │◄───────────────────────────┤
    │                            │                            │
    │ 4. Return user (no pwd)    │                            │
    │◄───────────────────────────┤                            │
    │                            │                            │
```

### 2. Login Flow
```
┌────────┐                  ┌─────────┐                 ┌───────────┐
│ Client │                  │ Gateway │                 │ Client-MS │
└───┬────┘                  └────┬────┘                 └─────┬─────┘
    │                            │                            │
    │ POST /auth/login           │                            │
    │ {email, password}          │                            │
    ├───────────────────────────►│                            │
    │                            │                            │
    │                            │ 1. GET /api/v1/users/      │
    │                            │    email/{email}           │
    │                            ├───────────────────────────►│
    │                            │                            │
    │                            │ 2. User data               │
    │                            │◄───────────────────────────┤
    │                            │                            │
    │                            │ 3. Validate password       │
    │                            │    (BCrypt.matches)        │
    │                            │                            │
    │                            │ 4. Generate UUID token     │
    │                            │                            │
    │                            │ 5. Store in TokenService   │
    │                            │    Map<token, TokenInfo>   │
    │                            │                            │
    │ 6. Return token + user     │                            │
    │◄───────────────────────────┤                            │
    │                            │                            │
```

### 3. Authenticated Request Flow
```
┌────────┐                  ┌─────────┐                 ┌───────────┐
│ Client │                  │ Gateway │                 │ Client-MS │
└───┬────┘                  └────┬────┘                 └─────┬─────┘
    │                            │                            │
    │ GET /MS-CLIENT/users/123   │                            │
    │ Authorization: Bearer xyz  │                            │
    ├───────────────────────────►│                            │
    │                            │                            │
    │                            │ 1. AuthenticationFilter    │
    │                            │    - Extract token         │
    │                            │    - Validate token        │
    │                            │    - Get user from token   │
    │                            │                            │
    │                            │ 2. Add headers:            │
    │                            │    X-User-Id: 123          │
    │                            │    X-User-Email: user@...  │
    │                            │    X-User-Role: CLIENT     │
    │                            │                            │
    │                            │ 3. Forward request         │
    │                            ├───────────────────────────►│
    │                            │                            │
    │                            │ 4. Response                │
    │                            │◄───────────────────────────┤
    │                            │                            │
    │ 5. Response to client      │                            │
    │◄───────────────────────────┤                            │
    │                            │                            │
```

### 4. Admin Authorization Flow
```
┌────────┐                  ┌─────────┐                 ┌───────────┐
│ Client │                  │ Gateway │                 │ Client-MS │
└───┬────┘                  └────┬────┘                 └─────┬─────┘
    │                            │                            │
    │ GET /MS-CLIENT/users       │                            │
    │ Authorization: Bearer xyz  │                            │
    ├───────────────────────────►│                            │
    │                            │                            │
    │                            │ 1. AuthenticationFilter    │
    │                            │    - Validate token        │
    │                            │    - Add X-User-Role       │
    │                            │                            │
    │                            │ 2. AdminAuthorizationFilter│
    │                            │    - Check X-User-Role     │
    │                            │    - If ADMIN: continue    │
    │                            │    - Else: return 403      │
    │                            │                            │
    │                            │ 3. Forward (if admin)      │
    │                            ├───────────────────────────►│
    │                            │                            │
    │                            │ 4. Response                │
    │                            │◄───────────────────────────┤
    │                            │                            │
    │ 5. Response to client      │                            │
    │◄───────────────────────────┤                            │
    │                            │                            │
```

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     AuthController                           │
│  ┌──────────┐   ┌──────────────┐   ┌──────────────────┐    │
│  │ register │   │    login     │   │     logout       │    │
│  └────┬─────┘   └──────┬───────┘   └────────┬─────────┘    │
│       │                │                     │              │
│       │                │                     │              │
│       ▼                ▼                     ▼              │
│  ┌────────────────────────────────────────────────────┐    │
│  │            BCryptPasswordEncoder                    │    │
│  └────────────────────────────────────────────────────┘    │
│       │                │                     │              │
│       ▼                ▼                     ▼              │
│  ┌────────────────────────────────────────────────────┐    │
│  │              TokenService                           │    │
│  │  - generateToken()                                  │    │
│  │  - storeToken(token, userId, email, role)          │    │
│  │  - validateToken(token)                             │    │
│  │  - getUserFromToken(token)                          │    │
│  │  - invalidateToken(token)                           │    │
│  │                                                     │    │
│  │  Storage: ConcurrentHashMap<String, TokenInfo>     │    │
│  └────────────────────────────────────────────────────┘    │
│       │                                                     │
│       ▼                                                     │
│  ┌────────────────────────────────────────────────────┐    │
│  │           UserServiceClient (Feign)                 │    │
│  │  - getUserByEmail(email)                            │    │
│  │  - createUser(userDTO)                              │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP calls via Eureka
                            ▼
                    ┌───────────────┐
                    │   Client-MS   │
                    │  (MS-CLIENT)  │
                    └───────────────┘
```

## Data Flow: Token Storage

```
┌──────────────────────────────────────────────────────────┐
│              TokenService (In-Memory)                     │
│                                                           │
│  ConcurrentHashMap<String, TokenInfo>                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │                                                  │    │
│  │  "uuid-1234" → TokenInfo {                      │    │
│  │                  userId: 1,                      │    │
│  │                  email: "user@example.com",      │    │
│  │                  role: "CLIENT",                 │    │
│  │                  createdAt: 2025-11-19T10:00:00  │    │
│  │                }                                 │    │
│  │                                                  │    │
│  │  "uuid-5678" → TokenInfo {                      │    │
│  │                  userId: 2,                      │    │
│  │                  email: "admin@example.com",     │    │
│  │                  role: "ADMIN",                  │    │
│  │                  createdAt: 2025-11-19T10:05:00  │    │
│  │                }                                 │    │
│  │                                                  │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
│  Operations:                                             │
│  • put(token, tokenInfo) - On login                      │
│  • get(token) - On each request validation               │
│  • remove(token) - On logout                             │
│  • containsKey(token) - Token validation                 │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

## Route Filtering Pipeline

```
Request → Gateway
    │
    ├─ Route: /auth/** (Public)
    │    └─ No filters → AuthController
    │
    ├─ Route: /product-service/** GET (Public)
    │    └─ CustomGatewayFilter → product-service
    │
    ├─ Route: /product-service/** POST/PUT/DELETE (Protected)
    │    └─ AuthenticationFilter → CustomGatewayFilter → product-service
    │
    ├─ Route: /MS-CLIENT/users/** GET (Protected)
    │    └─ AuthenticationFilter → MS-CLIENT
    │
    ├─ Route: /MS-CLIENT/users GET (Admin)
    │    └─ AuthenticationFilter → AdminAuthorizationFilter → MS-CLIENT
    │
    └─ Route: /MS-CLIENT/users/** PUT/DELETE (Admin)
         └─ AuthenticationFilter → AdminAuthorizationFilter → MS-CLIENT
```

## Security Layers

```
┌──────────────────────────────────────────────────────────┐
│  Layer 1: Route Matching                                  │
│  - Determine if route is public or protected             │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────┐
│  Layer 2: Authentication (AuthenticationFilter)           │
│  - Extract Bearer token                                   │
│  - Validate token exists in TokenService                  │
│  - Retrieve user information                              │
│  - Add X-User-* headers                                   │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────┐
│  Layer 3: Authorization (AdminAuthorizationFilter)        │
│  - Check X-User-Role header                               │
│  - Verify ADMIN role for admin routes                     │
│  - Return 403 if unauthorized                             │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────┐
│  Layer 4: Forward to Microservice                         │
│  - Request includes user context headers                  │
│  - Microservice can trust the headers                     │
└──────────────────────────────────────────────────────────┘
```

## Error Handling Flow

```
Exception → GlobalExceptionHandler
    │
    ├─ UnauthorizedException
    │    └─ HTTP 401 + JSON error response
    │
    ├─ ForbiddenException
    │    └─ HTTP 403 + JSON error response
    │
    └─ Generic Exception
         └─ HTTP 500 + JSON error response

Response Format:
{
  "timestamp": "2025-11-19T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

This architecture ensures:
✅ Centralized authentication at Gateway
✅ Token-based stateless authentication
✅ Role-based access control
✅ User context propagation to microservices
✅ Consistent error handling
✅ Scalable and maintainable design

