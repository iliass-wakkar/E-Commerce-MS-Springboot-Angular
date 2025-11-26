# Gateway Microservice - Frontend Integration Guide

## Base Configuration

### Gateway Details
- **Base URL**: `http://localhost:1111`
- **Protocol**: HTTP (use HTTPS in production)
- **CORS**: Configured for `http://localhost:4200`
- **Authentication**: Token-based (Bearer token)

### Required Headers
```typescript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer <token>' // For protected endpoints
}
```

---

## Authentication Endpoints

### 1. Login
**Endpoint**: `POST /auth/login`

**Purpose**: Authenticate user and receive access token

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "userPassword123"
}
```

**Success Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "role": "CLIENT"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/auth/login"
}
```

**Frontend Implementation**:
```typescript
// auth.service.ts
login(email: string, password: string): Observable<LoginResponse> {
  return this.http.post<LoginResponse>(
    `${environment.apiBaseUrl}/auth/login`,
    { email, password }
  );
}
```

---

### 2. Register
**Endpoint**: `POST /auth/register`

**Purpose**: Create a new user account

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "+1234567890",
  "role": "CLIENT"  // Optional, defaults to "CLIENT"
}
```

**Success Response** (201 Created):
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

**Error Response** (400 Bad Request):
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists",
  "path": "/auth/register"
}
```

---

### 3. Logout
**Endpoint**: `POST /auth/logout`

**Purpose**: Invalidate user token

**Required Headers**:
```
Authorization: Bearer <token>
```

**Request Body**: Empty

**Success Response** (200 OK):
```json
{
  "message": "Logged out successfully"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/auth/logout"
}
```

---

## Protected Resource Routes

All routes below require the `Authorization` header with a valid Bearer token.

### Format
```
Authorization: Bearer <token>
```

### Common Protected Endpoints
(These are forwarded by the gateway to respective microservices)

- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product (ADMIN only)
- `PUT /api/products/{id}` - Update product (ADMIN only)
- `DELETE /api/products/{id}` - Delete product (ADMIN only)

- `GET /api/orders` - Get user orders
- `POST /api/orders` - Create new order
- `GET /api/orders/{id}` - Get order details

- `GET /api/users/{id}` - Get user profile (ADMIN or owner)
- `PUT /api/users/{id}` - Update user profile (ADMIN or owner)

---

## Angular Environment Configuration

### environment.development.ts
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:1111'
};
```

### environment.ts (production)
```typescript
export const environment = {
  production: true,
  apiBaseUrl: 'https://your-production-gateway.com'
};
```

---

## Angular Service Implementation

### auth.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../environments/environment';

interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  token: string;
  userId: number;
  email: string;
  role: string;
}

interface RegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
  telephone: string;
  role?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<LoginResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    // Load user from storage on init
    const storedUser = sessionStorage.getItem('currentUser');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, { email, password })
      .pipe(
        tap(response => {
          sessionStorage.setItem('currentUser', JSON.stringify(response));
          sessionStorage.setItem('token', response.token);
          this.currentUserSubject.next(response);
        })
      );
  }

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, data);
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {})
      .pipe(
        tap(() => {
          sessionStorage.removeItem('currentUser');
          sessionStorage.removeItem('token');
          this.currentUserSubject.next(null);
        })
      );
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): LoginResponse | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }
}
```

---

## HTTP Interceptor for Authentication

### auth.interceptor.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Add token to request if available
    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Unauthorized - redirect to login
          this.authService.logout().subscribe();
          this.router.navigate(['/login']);
        } else if (error.status === 403) {
          // Forbidden - show access denied
          this.router.navigate(['/access-denied']);
        } else if (error.status === 0) {
          // Network error or CORS issue
          console.error('Network error or CORS issue:', error);
        }
        return throwError(() => error);
      })
    );
  }
}
```

### Register Interceptor in app.config.ts (Standalone)
```typescript
import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AuthInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([AuthInterceptor])
    )
  ]
};
```

### Or in app.module.ts (Module-based)
```typescript
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ]
})
export class AppModule { }
```

---

## Route Guards

### auth.guard.ts
```typescript
import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.authService.isLoggedIn()) {
      // Check if route requires specific role
      const requiredRole = route.data['role'];
      if (requiredRole && !this.authService.hasRole(requiredRole)) {
        this.router.navigate(['/access-denied']);
        return false;
      }
      return true;
    }

    // Not logged in, redirect to login
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
}
```

### Usage in Routes
```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent, 
    canActivate: [AuthGuard] 
  },
  { 
    path: 'admin', 
    component: AdminComponent, 
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' }
  }
];
```

---

## Error Handling

### Standard Error Format
All errors from the gateway follow this format:
```json
{
  "timestamp": "2025-11-23T10:15:30.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/auth/login"
}
```

### Error Status Codes
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (invalid/missing token or credentials)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource doesn't exist)
- `500` - Internal Server Error

---

## Token Management

### Token Storage
- **Recommended**: Use `sessionStorage` for better security
- **Alternative**: Use `localStorage` for persistent sessions (less secure)
- **Best Practice**: Never store tokens in cookies without HttpOnly flag

### Token Lifecycle
- Token TTL: 86400 seconds (24 hours)
- Token is validated on each request
- Expired tokens return 401 error
- User must re-login after token expiration

---

## Testing with Postman

### Login Request
```
POST http://localhost:1111/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

### Protected Request
```
GET http://localhost:1111/api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Common Issues & Solutions

### Issue: CORS Error
**Solution**: Gateway CORS is configured for `http://localhost:4200`. If using different port, update `application.yml`:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:YOUR_PORT"
```

### Issue: 401 Unauthorized on Protected Routes
**Solution**: Ensure token is included in Authorization header with "Bearer " prefix

### Issue: Network Error (Status 0)
**Possible Causes**:
1. Gateway not running
2. Wrong port number
3. CORS misconfiguration
4. Network connectivity issue

**Solution**: 
- Verify gateway is running on port 1111
- Check browser console for detailed error
- Ensure CORS configuration is correct

---

## Production Checklist

- [ ] Change `apiBaseUrl` to production gateway URL
- [ ] Enable HTTPS
- [ ] Configure production CORS origins
- [ ] Implement token refresh mechanism
- [ ] Add request/response logging
- [ ] Implement retry logic for failed requests
- [ ] Add loading indicators
- [ ] Handle offline scenarios
- [ ] Implement proper error notifications
- [ ] Consider using HTTP-only cookies for tokens
- [ ] Enable CSRF protection if using cookies
- [ ] Add request timeout handling

---

## Support & Contact

For issues related to the Gateway microservice, contact the backend team.

**Gateway Repository**: [Your Repo URL]
**API Documentation**: Available at `/swagger-ui` (if configured)
**Health Check**: `GET http://localhost:1111/actuator/health`

