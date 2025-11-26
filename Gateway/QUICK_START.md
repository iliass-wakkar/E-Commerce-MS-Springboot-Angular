# Quick Start Guide - Gateway Authentication

## 🚀 Running the System

### Prerequisites
1. Eureka Server running (default port: 8761)
2. Client-MS microservice running (should register with Eureka as "MS-CLIENT")
3. Gateway configured and compiled ✅

### Start the Gateway
```bash
cd "C:\Users\ilias\Desktop\EMSI\springboot\JEE2 project\MS project\Gateway"
.\mvnw.cmd spring-boot:run
```

Gateway will start on: **http://localhost:1111**

## 📝 Quick Test Commands (PowerShell)

### 1. Register a Client User
```powershell
$body = @{
    nom = "Jane"
    prenom = "Doe"
    email = "jane.doe@example.com"
    password = "password123"
    telephone = "0612345678"
    role = "CLIENT"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:1111/auth/register" -Method POST -Body $body -ContentType "application/json"
```

### 2. Register an Admin User
```powershell
$body = @{
    nom = "Admin"
    prenom = "User"
    email = "admin@example.com"
    password = "admin123"
    telephone = "0612345679"
    role = "ADMIN"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:1111/auth/register" -Method POST -Body $body -ContentType "application/json"
```

### 3. Login as Client
```powershell
$loginBody = @{
    email = "jane.doe@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:1111/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$clientToken = $loginResponse.token
Write-Host "Client Token: $clientToken"
```

### 4. Login as Admin
```powershell
$adminLoginBody = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json

$adminLoginResponse = Invoke-RestMethod -Uri "http://localhost:1111/auth/login" -Method POST -Body $adminLoginBody -ContentType "application/json"
$adminToken = $adminLoginResponse.token
Write-Host "Admin Token: $adminToken"
```

### 5. Access Protected Resource (Client)
```powershell
$headers = @{
    "Authorization" = "Bearer $clientToken"
}
Invoke-RestMethod -Uri "http://localhost:1111/MS-CLIENT/users/1" -Method GET -Headers $headers
```

### 6. Try Admin Route as Client (Should Fail - 403)
```powershell
$headers = @{
    "Authorization" = "Bearer $clientToken"
}
try {
    Invoke-RestMethod -Uri "http://localhost:1111/MS-CLIENT/users" -Method GET -Headers $headers
} catch {
    Write-Host "Expected 403 Forbidden: $_"
}
```

### 7. Access Admin Route as Admin (Should Succeed)
```powershell
$headers = @{
    "Authorization" = "Bearer $adminToken"
}
Invoke-RestMethod -Uri "http://localhost:1111/MS-CLIENT/users" -Method GET -Headers $headers
```

### 8. Logout
```powershell
$headers = @{
    "Authorization" = "Bearer $clientToken"
}
Invoke-RestMethod -Uri "http://localhost:1111/auth/logout" -Method POST -Headers $headers
```

## 🌐 Using cURL (Git Bash or WSL)

### Register
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

### Login
```bash
curl -X POST http://localhost:1111/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### Access Protected Resource
```bash
# Replace YOUR_TOKEN with the token from login response
curl -X GET http://localhost:1111/MS-CLIENT/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Logout
```bash
curl -X POST http://localhost:1111/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🧪 Using Postman

### Setup
1. Create a new Postman collection called "Gateway Auth"
2. Add environment variables:
   - `gateway_url`: `http://localhost:1111`
   - `client_token`: (will be set after login)
   - `admin_token`: (will be set after admin login)

### Register Request
- **Method**: POST
- **URL**: `{{gateway_url}}/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "nom": "Test",
  "prenom": "User",
  "email": "test@example.com",
  "password": "test123",
  "telephone": "0612345678",
  "role": "CLIENT"
}
```

### Login Request
- **Method**: POST
- **URL**: `{{gateway_url}}/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "test123"
}
```
- **Tests** tab (to save token):
```javascript
var jsonData = pm.response.json();
pm.environment.set("client_token", jsonData.token);
```

### Protected Request
- **Method**: GET
- **URL**: `{{gateway_url}}/MS-CLIENT/users/1`
- **Headers**: `Authorization: Bearer {{client_token}}`

### Admin Request
- **Method**: GET
- **URL**: `{{gateway_url}}/MS-CLIENT/users`
- **Headers**: `Authorization: Bearer {{admin_token}}`

### Logout Request
- **Method**: POST
- **URL**: `{{gateway_url}}/auth/logout`
- **Headers**: `Authorization: Bearer {{client_token}}`

## 🔍 Troubleshooting

### Gateway won't start
- Check if port 1111 is available
- Ensure Eureka Server is running
- Check application.yml configuration

### 401 Unauthorized
- Verify token is included in Authorization header
- Check token format: `Bearer <token>`
- Ensure you logged in successfully

### 403 Forbidden
- Verify user has required role (ADMIN for admin routes)
- Check token is valid and not expired

### Cannot connect to Client-MS
- Ensure Client-MS is registered with Eureka
- Check Eureka dashboard: http://localhost:8761
- Verify service name is "MS-CLIENT"

### Feign Client errors
- Check if Client-MS endpoints match the Feign interface
- Verify Client-MS is running and healthy
- Check logs for connection errors

## 📊 Monitoring

### Check Active Tokens
The TokenService keeps tokens in memory. You can add an admin endpoint to monitor active sessions.

### Eureka Dashboard
Visit: http://localhost:8761
- Verify "REACTIVEGATEWAY" is registered
- Verify "MS-CLIENT" is registered

### Gateway Logs
Check for:
- Authentication filter logs
- Feign client calls
- Route matching

## 🎯 Common Use Cases

### Create a new user and login
1. POST /auth/register → Create user
2. POST /auth/login → Get token
3. Use token for subsequent requests

### Access user profile
1. Login → Get token
2. GET /MS-CLIENT/users/{id} with token

### Admin operations
1. Login as admin → Get admin token
2. GET /MS-CLIENT/users → List all users
3. PUT /MS-CLIENT/users/{id} → Update user
4. DELETE /MS-CLIENT/users/{id} → Delete user

### Logout
1. POST /auth/logout with token
2. Token is invalidated
3. Further requests with that token will fail

## ✅ Success Indicators

When everything is working:
- ✅ Gateway starts without errors
- ✅ Registration returns user data (without password)
- ✅ Login returns token + user info
- ✅ Protected routes accept valid tokens
- ✅ Admin routes reject non-admin users
- ✅ Invalid tokens return 401
- ✅ Logout invalidates tokens

For detailed documentation, see **AUTHENTICATION.md**

