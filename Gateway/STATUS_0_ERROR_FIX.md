# HTTP Status 0 Error - Troubleshooting Guide

## What is Status 0 Error?

**Status 0** means the HTTP request **never reached the backend server**. The browser blocked it before it could even start.

## Root Causes (In Order of Likelihood):

### 1. ✅ **CORS Issue** (Most Common)
**Symptom**: Request blocked by browser's CORS policy
**Solution**: Fixed by updating `CorsConfig.java`

### 2. 🔴 **Backend Server Not Running**
**Symptom**: Gateway is not started
**Solution**: Start the Gateway application

### 3. 🌐 **Wrong URL**
**Symptom**: Frontend hitting wrong port or hostname
**Solution**: Verify URL is `http://localhost:1111/auth/login`

### 4. 🔥 **Firewall Blocking**
**Symptom**: Windows Firewall blocking port 1111
**Solution**: Allow Java through firewall

---

## Fixes Applied

### ✅ Fixed CORS Configuration

**Problem**: Duplicate CORS configs causing conflicts

**Before**:
- CORS in both `CorsConfig.java` AND `application.yml`
- Conflicting settings
- Missing proper headers

**After**:
- Single CORS configuration in `CorsConfig.java`
- Proper reactive CORS handling
- Explicit method listing (GET, POST, PUT, DELETE, OPTIONS)
- Exposed headers for JWT
- Max age for preflight cache

### Updated Files:

#### 1. `CorsConfig.java` ✅
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedOrigins(List.of("http://localhost:4200"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
}
```

#### 2. `application.yml` ✅
Removed duplicate CORS configuration to avoid conflicts.

---

## How to Verify the Fix

### Step 1: Start the Gateway
```bash
cd "C:\Users\ilias\Desktop\EMSI\springboot\JEE2 project\MS project\Gateway"
.\mvnw.cmd spring-boot:run
```

### Step 2: Check Gateway is Running
Open browser: `http://localhost:1111/actuator/health`

Should see: `{"status":"UP"}`

### Step 3: Test Login Endpoint
```bash
# PowerShell
$body = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:1111/auth/login" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

### Step 4: Check Browser Console
Open Chrome DevTools (F12) → Network tab → Try login

**Success**: Status 200 or 401 (means CORS is working, request reached server)
**Failure**: Status 0 (means CORS still blocking or server not running)

---

## Browser Developer Tools Debugging

### Chrome DevTools → Network Tab:

#### ✅ Success (Request reaches server):
```
Status: 200 OK  or  401 Unauthorized
Response Headers:
  access-control-allow-origin: http://localhost:4200
  access-control-allow-credentials: true
```

#### ❌ Status 0 Error (CORS blocked):
```
Status: (failed) net::ERR_FAILED
OR
Status: 0
Console Error: "Access to XMLHttpRequest at 'http://localhost:1111/auth/login' 
from origin 'http://localhost:4200' has been blocked by CORS policy"
```

### Chrome DevTools → Console Tab:

#### Look for CORS errors like:
```
Access to XMLHttpRequest at 'http://localhost:1111/auth/login' from origin 
'http://localhost:4200' has been blocked by CORS policy: Response to 
preflight request doesn't pass access control check: No 
'Access-Control-Allow-Origin' header is present on the requested resource.
```

---

## Common Frontend Issues

### Issue 1: Not Sending Credentials
**Problem**: Frontend not including credentials in request
**Solution**: Add `withCredentials: true` to Angular HTTP request

```typescript
// Angular HttpClient
this.http.post('http://localhost:1111/auth/login', body, {
  withCredentials: true  // ← Add this!
})
```

### Issue 2: Incorrect Content-Type
**Problem**: Not setting `Content-Type: application/json`
**Solution**: HttpClient sets it automatically, but verify

```typescript
const headers = new HttpHeaders({
  'Content-Type': 'application/json'
});

this.http.post(url, body, { headers })
```

### Issue 3: Using HttpParams Instead of Body
**Problem**: Sending data as URL params instead of JSON body
**Solution**: Use request body for POST

```typescript
// ❌ Wrong
this.http.post(url, null, { params: { email, password } })

// ✅ Correct
this.http.post(url, { email, password })
```

---

## Gateway Startup Checklist

### Before Testing:

- [ ] Gateway application is running on port 1111
- [ ] Eureka server is running (if using service discovery)
- [ ] Config server is running (if using config server)
- [ ] User service (MS-CLIENT) is running and registered
- [ ] No other application using port 1111
- [ ] Windows Firewall allows Java through port 1111

### Check Services:
```powershell
# Check if port 1111 is listening
netstat -ano | findstr :1111

# Should show something like:
# TCP    0.0.0.0:1111    0.0.0.0:0    LISTENING    12345
```

---

## Expected Behavior After Fix

### Preflight Request (OPTIONS):
```
Request:
  Method: OPTIONS
  URL: http://localhost:1111/auth/login
  Headers:
    Origin: http://localhost:4200
    Access-Control-Request-Method: POST

Response:
  Status: 200 OK
  Headers:
    Access-Control-Allow-Origin: http://localhost:4200
    Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
    Access-Control-Allow-Headers: *
    Access-Control-Allow-Credentials: true
```

### Actual Login Request (POST):
```
Request:
  Method: POST
  URL: http://localhost:1111/auth/login
  Headers:
    Content-Type: application/json
    Origin: http://localhost:4200
  Body:
    {"email":"user@example.com","password":"password123"}

Response:
  Status: 200 OK  (or 401 if credentials wrong)
  Headers:
    Access-Control-Allow-Origin: http://localhost:4200
    Access-Control-Allow-Credentials: true
  Body:
    {"token":"eyJhbGc...","userId":1,"email":"user@example.com","role":"CLIENT","expiresIn":86400}
```

---

## Still Getting Status 0?

### Try These:

1. **Clear Browser Cache**
   - Chrome: Ctrl+Shift+Delete → Clear cached images and files
   - Or use Incognito mode

2. **Restart Gateway**
   - Stop the application
   - Run `.\mvnw.cmd clean package`
   - Start again

3. **Check Gateway Logs**
   Look for CORS-related errors in console output

4. **Test with cURL** (Bypass browser CORS)
   ```bash
   curl -X POST http://localhost:1111/auth/login \
     -H "Content-Type: application/json" \
     -H "Origin: http://localhost:4200" \
     -d '{"email":"test@example.com","password":"password123"}' \
     -v
   ```
   
   If cURL works but browser doesn't → CORS issue
   If cURL fails → Backend issue

5. **Disable Browser CORS** (Testing only!)
   ```bash
   # Chrome with CORS disabled (Windows)
   "C:\Program Files\Google\Chrome\Application\chrome.exe" --disable-web-security --user-data-dir="C:/ChromeDevSession"
   ```
   **Warning**: Only for testing! Never use for normal browsing.

---

## Summary

✅ **CORS configuration has been fixed!**

- Removed duplicate configs
- Properly configured reactive CORS
- Added all necessary headers
- Set correct origins and methods

**Next Steps:**
1. Restart your Gateway application
2. Try login from frontend
3. Check browser DevTools Network tab
4. Should now see Status 200 or 401 (not 0)

If still getting Status 0, check that:
- Gateway is actually running
- Port 1111 is accessible
- Frontend is using correct URL

