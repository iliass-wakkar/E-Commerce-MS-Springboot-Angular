# Client Microservice API Documentation

## Base URL
```
http://localhost:<port>/api/v1
```

## Endpoints

### User Management Endpoints

#### 1. Create User (Register)
**POST** `/users`

Creates a new user with CLIENT role by default.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890"
}
```

**Response:** `201 CREATED`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890",
  "createdAt": "2025-11-19T12:00:00"
}
```

---

#### 2. Get All Users
**GET** `/users`

Retrieves all users in the system.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "CLIENT",
    "shippingAddress": "123 Main St, City",
    "phone": "+1234567890",
    "createdAt": "2025-11-19T12:00:00"
  },
  {
    "id": 2,
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@ecommerce.com",
    "role": "ADMIN",
    "shippingAddress": "N/A",
    "phone": "N/A",
    "createdAt": "2025-11-19T10:00:00"
  }
]
```

---

#### 3. Get User by ID
**GET** `/users/{id}`

Retrieves a specific user by their ID.

**Path Parameters:**
- `id` (Long) - The user ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890",
  "createdAt": "2025-11-19T12:00:00"
}
```

**Error Response:** `404 NOT FOUND`
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999"
}
```

---

#### 4. Get User by Email
**GET** `/users/email/{email}`

Retrieves a user by their email address. This endpoint is primarily used by the Gateway for authentication.

**Path Parameters:**
- `email` (String) - The user's email address

**Response:** `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "$2a$10$encrypted_password_hash",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890",
  "createdAt": "2025-11-19T12:00:00"
}
```

**Error Response:** `404 NOT FOUND`

---

#### 5. Update User
**PUT** `/users/{id}`

Updates user information (firstName, lastName, shippingAddress, phone).

**Path Parameters:**
- `id` (Long) - The user ID

**Request Body:**
```json
{
  "firstName": "John Updated",
  "lastName": "Doe Updated",
  "shippingAddress": "456 New St, City",
  "phone": "+0987654321"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "firstName": "John Updated",
  "lastName": "Doe Updated",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "456 New St, City",
  "phone": "+0987654321",
  "createdAt": "2025-11-19T12:00:00"
}
```

**Error Response:** `404 NOT FOUND`
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999"
}
```

---

#### 6. Delete User
**DELETE** `/users/{id}`

Deletes a user from the system.

**Path Parameters:**
- `id` (Long) - The user ID

**Response:** `204 NO CONTENT`

**Error Response:** `404 NOT FOUND`
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999"
}
```

---

### Admin Endpoints

All admin endpoints are under `/admin` path. The Gateway should enforce ADMIN role verification.

#### 7. Get All Users (Admin)
**GET** `/admin/users`

Retrieves all users. Same as GET `/users` but through admin route.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "CLIENT",
    "shippingAddress": "123 Main St, City",
    "phone": "+1234567890",
    "createdAt": "2025-11-19T12:00:00"
  }
]
```

---

#### 8. Get User by ID (Admin)
**GET** `/admin/users/{id}`

Retrieves a specific user. Same as GET `/users/{id}` but through admin route.

**Path Parameters:**
- `id` (Long) - The user ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890",
  "createdAt": "2025-11-19T12:00:00"
}
```

---

#### 9. Update User (Admin)
**PUT** `/admin/users/{id}`

Admin can update user information including role.

**Path Parameters:**
- `id` (Long) - The user ID

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890",
  "role": "ADMIN"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "ADMIN",
  "shippingAddress": "123 Main St, City",
  "phone": "+1234567890",
  "createdAt": "2025-11-19T12:00:00"
}
```

---

#### 10. Delete User (Admin)
**DELETE** `/admin/users/{id}`

Admin deletes a user from the system.

**Path Parameters:**
- `id` (Long) - The user ID

**Response:** `204 NO CONTENT`

---

## Error Responses

### Common Error Formats

#### 404 Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999"
}
```

#### 409 Conflict (Email Already Exists)
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email 'john.doe@example.com' is already taken."
}
```

#### 500 Internal Server Error
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Data Models

### UserCreateDTO
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "email": "string (required, unique)",
  "password": "string (required)",
  "shippingAddress": "string (required)",
  "phone": "string (required)"
}
```

### UserUpdateDTO
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "shippingAddress": "string (required)",
  "phone": "string (required)"
}
```

### AdminUserUpdateDTO
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "shippingAddress": "string (required)",
  "phone": "string (required)",
  "role": "CLIENT | ADMIN (optional)"
}
```

### UserResponseDTO
```json
{
  "id": "Long",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "role": "CLIENT | ADMIN",
  "shippingAddress": "string",
  "phone": "string",
  "createdAt": "timestamp"
}
```

### User (Full Entity - includes password)
```json
{
  "id": "Long",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "password": "string (BCrypt hashed)",
  "role": "CLIENT | ADMIN",
  "shippingAddress": "string",
  "phone": "string",
  "createdAt": "timestamp"
}
```

---

## Notes

1. **No Authentication**: This microservice does not handle authentication. All authentication and authorization should be handled at the Gateway level.

2. **Password Hashing**: Passwords are hashed using BCrypt before storing in the database.

3. **Default User**: An admin user is created on application startup:
   - Email: `admin@ecommerce.com`
   - Password: `adminpassword`
   - Role: `ADMIN`

4. **Gateway Integration**: The Gateway should:
   - Handle login/logout
   - Verify user credentials using GET `/users/email/{email}`
   - Add user ID to request headers when forwarding to this service
   - Protect admin endpoints based on user role

5. **Email Uniqueness**: Email addresses must be unique in the system.

6. **Role Management**: Only admins can change user roles using the admin update endpoint.

