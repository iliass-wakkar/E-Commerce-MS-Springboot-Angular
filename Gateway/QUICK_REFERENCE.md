# Quick Reference - Gateway Authentication

## ✅ CORS Problem - FIXED

### What Changed
1. ✅ Added CORS configuration to allow `http://localhost:4200`
2. ✅ Added Spring Security for reactive gateway
3. ✅ Created `CorsConfig.java`
4. ✅ Updated `SecurityConfig.java` to permit OPTIONS and auth endpoints
5. ✅ Build successful

---

## 🔧 Frontend Changes Required

### 1. Update Base URL
```typescript
// environment.development.ts
export const environment = {
  apiBaseUrl: 'http://localhost:1111'  // Changed from 8081 or other port
};
```

### 2. Update Login Endpoint
```typescript
// OLD
POST http://localhost:8081/api/auth/login

// NEW
POST http://localhost:1111/auth/login
```

### 3. Login Request Body
```json
{
  "email": "user@example.com",     // Use "email" not "username"
  "password": "password123"
}
```

### 4. Login Response
```json
{
  "token": "uuid-token",
  "userId": 1,
  "email": "user@example.com",
  "role": "CLIENT"
}
```

---

## 🎯 Gateway Endpoints

| Method | Endpoint | Auth Required | Purpose |
|--------|----------|---------------|---------|
| POST | `/auth/login` | No | Login and get token |
| POST | `/auth/register` | No | Create new account |
| POST | `/auth/logout` | Yes | Invalidate token |
| GET/POST | `/api/**` | Yes | Protected resources |

---

## 🔐 Using the Token

### Store Token
```typescript
sessionStorage.setItem('token', response.token);
```

### Add to Requests
```typescript
headers: {
  'Authorization': `Bearer ${token}`
}
```

---

## 🧪 Test It

### From Browser Console
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
.then(console.log);
```

### Expected Success
```json
{
  "token": "generated-token",
  "userId": 1,
  "email": "test@example.com",
  "role": "CLIENT"
}
```

---

## 📋 Checklist

### Backend (Gateway)
- [x] CORS configured
- [x] Security configured
- [x] Auth endpoints working
- [x] Build successful

### Frontend (Your Todo)
- [ ] Update `environment.ts` base URL
- [ ] Change login endpoint path
- [ ] Use `email` field instead of `username`
- [ ] Store token on login success
- [ ] Add Authorization header to requests
- [ ] Test login flow
- [ ] Test protected routes

---

## 📚 Documentation Files

1. **CORS_FIX_SUMMARY.md** - This problem & solution
2. **FRONTEND_INTEGRATION.md** - Complete Angular integration guide
3. **API_ENDPOINTS_SUMMARY.md** - All endpoints reference
4. **proxy.conf.json** - Angular proxy config (optional)

---

## 🚀 Start Gateway

```bash
cd "C:\Users\ilias\Desktop\EMSI\springboot\JEE2 project\MS project\Gateway"
.\mvnw.cmd spring-boot:run
```

Gateway will start on: **http://localhost:1111**

---

## ⚠️ Common Issues

### Issue: Still getting CORS error
**Solution**: 
1. Restart Gateway after changes
2. Clear browser cache
3. Check browser console for actual error
4. Verify you're calling `http://localhost:1111` not `4200`

### Issue: 401 Unauthorized
**Solution**:
1. Check if token is in Authorization header
2. Verify token format: `Bearer <token>`
3. Check if token is expired

### Issue: 404 Not Found
**Solution**:
1. Verify endpoint path is correct
2. Gateway must be running
3. Check if URL has correct port (1111)

---

## 💡 Pro Tip

Use Angular HTTP Interceptor to automatically add the token:

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
```

---

## Need Help?

See the detailed documentation files created in the Gateway project folder.

