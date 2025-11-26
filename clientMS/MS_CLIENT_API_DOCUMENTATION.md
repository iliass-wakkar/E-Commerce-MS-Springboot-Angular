# MS-CLIENT Microservice - API Documentation

## Service Information
- **Service Name**: `ms-client`
- **Default Port**: `8080`
- **Base URL**: `http://localhost:8080`
- **Gateway URL** (if using API Gateway): `http://localhost:1111/MS-CLIENT`

---

## Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [User Management Endpoints](#user-management-endpoints)
3. [Data Models](#data-models)
4. [Error Responses](#error-responses)

---

## Authentication Endpoints

### 1. Register User
**Endpoint**: `POST /auth/register`

**Description**: Creates a new user account with CLIENT role by default.

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890"
}
```

**Response**: `201 CREATED`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

---

### 2. Login
**Endpoint**: `POST /auth/login`

**Description**: Authenticates a user by email and password.

**Request Body**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "$2a$10$...",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `401 Unauthorized`: Invalid credentials

---

### 3. Get Current User (Me)
**Endpoint**: `GET /auth/me?email={email}`

**Description**: Retrieves the current authenticated user's information.

**Query Parameters**:
- `email` (String, required): User's email address

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Email parameter is missing
- `404 Not Found`: User not found

---

### 4. Update Current User Profile
**Endpoint**: `PUT /auth/me?email={email}`

**Description**: Updates the current authenticated user's profile information.

**Query Parameters**:
- `email` (String, required): User's email address

**Request Body** (`UserUpdateDTO`):
```json
{
  "firstName": "John Updated",
  "lastName": "Doe",
  "shippingAddress": "456 New Address, City, Country",
  "phone": "+0987654321"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John Updated",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "456 New Address, City, Country",
  "phone": "+0987654321",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Email parameter is missing
- `404 Not Found`: User not found

---

### 5. Delete Current User Account
**Endpoint**: `DELETE /auth/me?email={email}`

**Description**: Deletes the current authenticated user's account.

**Query Parameters**:
- `email` (String, required): User's email address

**Response**: `204 No Content`

**Error Responses**:
- `400 Bad Request`: Email parameter is missing
- `404 Not Found`: User not found

---

## User Management Endpoints

### 1. Register New User
**Endpoint**: `POST /api/v1/users`

**Description**: Creates a new user account with CLIENT role by default.

**Request Body** (`UserCreateDTO`):
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890"
}
```

**Response**: `201 CREATED`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Email already exists

---

### 2. Get All Users
**Endpoint**: `GET /api/v1/users`

**Description**: Retrieves a list of all registered users.

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "CLIENT",
    "shippingAddress": "123 Main St, City, Country",
    "phone": "+1234567890",
    "createdAt": "2025-11-23T10:30:00Z"
  },
  {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "role": "ADMIN",
    "shippingAddress": "456 Oak Ave, City, Country",
    "phone": "+0987654321",
    "createdAt": "2025-11-23T11:00:00Z"
  }
]
```

---

### 3. Get User by ID
**Endpoint**: `GET /api/v1/users/{id}`

**Description**: Retrieves a specific user by their ID.

**Path Parameters**:
- `id` (Long): User ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `404 Not Found`: User not found with the given ID

---

### 4. Get User by Email
**Endpoint**: `GET /api/v1/users/email/{email}`

**Description**: Retrieves a user by their email address. Used primarily for authentication in the Gateway.

**Path Parameters**:
- `email` (String): User email address

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "$2a$10$...", // BCrypt hashed password
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `404 Not Found`: User not found with the given email

---

### 5. Update User (Self-Update)
**Endpoint**: `PUT /api/v1/users/{id}`

**Description**: Updates user information. Users can update their own profile (excluding role).

**Path Parameters**:
- `id` (Long): User ID

**Request Body** (`UserUpdateDTO`):
```json
{
  "firstName": "John Updated",
  "lastName": "Doe",
  "shippingAddress": "789 New St, City, Country",
  "phone": "+1111111111"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John Updated",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "789 New St, City, Country",
  "phone": "+1111111111",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `404 Not Found`: User not found with the given ID

---

### 6. Delete User
**Endpoint**: `DELETE /api/v1/users/{id}`

**Description**: Deletes a user account.

**Path Parameters**:
- `id` (Long): User ID

**Response**: `204 No Content`

**Error Responses**:
- `404 Not Found`: User not found with the given ID

---

## Admin Endpoints

### 1. Get All Users (Admin)
**Endpoint**: `GET /admin/users`

**Description**: Admin endpoint to retrieve all users.

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "CLIENT",
    "shippingAddress": "123 Main St, City, Country",
    "phone": "+1234567890",
    "createdAt": "2025-11-23T10:30:00Z"
  }
]
```

---

### 2. Get User by ID (Admin)
**Endpoint**: `GET /admin/users/{id}`

**Description**: Admin endpoint to retrieve a specific user.

**Path Parameters**:
- `id` (Long): User ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `404 Not Found`: User not found

---

### 3. Update User (Admin)
**Endpoint**: `PUT /admin/users/{id}`

**Description**: Admin endpoint to update any user's information, including their role.

**Path Parameters**:
- `id` (Long): User ID

**Request Body** (`AdminUserUpdateDTO`):
```json
{
  "firstName": "John",
  "lastName": "Doe Updated",
  "shippingAddress": "999 Admin St, City, Country",
  "phone": "+9999999999",
  "role": "ADMIN"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe Updated",
  "email": "john.doe@example.com",
  "role": "ADMIN",
  "shippingAddress": "999 Admin St, City, Country",
  "phone": "+9999999999",
  "createdAt": "2025-11-23T10:30:00Z"
}
```

**Error Responses**:
- `404 Not Found`: User not found

---

### 4. Delete User (Admin)
**Endpoint**: `DELETE /admin/users/{id}`

**Description**: Admin endpoint to delete any user account.

**Path Parameters**:
- `id` (Long): User ID

**Response**: `204 No Content`

**Error Responses**:
- `404 Not Found`: User not found

---

## Data Models

### UserCreateDTO
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "password": "string",
  "shippingAddress": "string",
  "phone": "string"
}
```

### UserUpdateDTO
```json
{
  "firstName": "string",
  "lastName": "string",
  "shippingAddress": "string",
  "phone": "string"
}
```

### UserResponseDTO
```json
{
  "id": "number",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "role": "CLIENT | ADMIN",
  "shippingAddress": "string",
  "phone": "string",
  "createdAt": "ISO-8601 timestamp"
}
```

### User (Full Entity - returned by email endpoint)
```json
{
  "id": "number",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "password": "string (BCrypt hashed)",
  "role": "CLIENT | ADMIN",
  "shippingAddress": "string",
  "phone": "string",
  "createdAt": "ISO-8601 timestamp"
}
```

### Role Enum
- `CLIENT`: Standard user role
- `ADMIN`: Administrator role with elevated privileges

---

## Error Responses

### Common Error Format
```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "status": 400
}
```

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| `200 OK` | Request successful |
| `201 Created` | Resource created successfully |
| `204 No Content` | Request successful, no content returned |
| `400 Bad Request` | Invalid request data or email already exists |
| `404 Not Found` | Resource not found |
| `500 Internal Server Error` | Server error |

### Exception Types

1. **EmailAlreadyExistsException**
   - Status: `400 Bad Request`
   - Message: "Email '{email}' is already taken."

2. **UserNotFoundException**
   - Status: `404 Not Found`
   - Message: "User not found with id: {id}"

---

## Testing with cURL

### Register a new user
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "shippingAddress": "123 Main St",
    "phone": "+1234567890"
  }'
```

### Get all users
```bash
curl -X GET http://localhost:8080/api/v1/users
```

### Get user by ID
```bash
curl -X GET http://localhost:8080/api/v1/users/1
```

### Update user
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John Updated",
    "lastName": "Doe",
    "shippingAddress": "456 New St",
    "phone": "+9876543210"
  }'
```

### Delete user
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

---

## Testing with Postman

### Collection Variables
- `baseUrl`: `http://localhost:8080`
- `gatewayUrl`: `http://localhost:1111/MS-CLIENT`

### Headers
```
Content-Type: application/json
Accept: application/json
```

---

## Security Configuration

**Current Status**: All endpoints are currently **publicly accessible** (no authentication required).

The security configuration permits all requests:
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()
)
```

**CSRF Protection**: Disabled for API development.

---

## Notes

1. **Password Encoding**: All passwords are encrypted using BCrypt before storage.
2. **Default Role**: New registrations are automatically assigned the `CLIENT` role.
3. **Role Changes**: Only admin endpoints can modify user roles.
4. **Email Uniqueness**: Email addresses must be unique across all users.
5. **Timestamps**: All timestamps use ISO-8601 format in UTC timezone.

---

## Gateway Integration

When accessed through the API Gateway on port `1111`, prepend `/MS-CLIENT` to all paths:

**Examples**:
- Direct: `http://localhost:8080/api/v1/users`
- Gateway: `http://localhost:1111/MS-CLIENT/api/v1/users`

---

## Future Enhancements

1. **Authentication**: JWT-based authentication
2. **Authorization**: Role-based access control (RBAC)
3. **Pagination**: Add pagination support for list endpoints
4. **Filtering**: Add query parameters for filtering users
5. **Validation**: Add comprehensive input validation
6. **Swagger/OpenAPI**: Auto-generated API documentation

