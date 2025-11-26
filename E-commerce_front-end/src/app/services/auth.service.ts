import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../environments/environment';

export interface User {
    id: string;
    username: string; // Mapped from email or nom+prenom
    email?: string;
    firstName?: string; // nom/prenom from backend
    lastName?: string;
    roles: string[];
    permissions?: string[];
}

export interface LoginResponse {
    token: string;
    userId: number;
    email: string;
    role: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authUrl = `${environment.apiUrl}/auth`;

    private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
    public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

    private userRoleSubject = new BehaviorSubject<string | null>(null);
    public userRole$ = this.userRoleSubject.asObservable();

    private currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(
        private http: HttpClient,
        private router: Router,
        @Inject(PLATFORM_ID) private platformId: Object
    ) {
        this.initializeAuth();
    }

    private initializeAuth() {
        if (isPlatformBrowser(this.platformId)) {
            const token = localStorage.getItem('accessToken');
            const userStr = localStorage.getItem('user');

            if (token && userStr) {
                try {
                    const user = JSON.parse(userStr);
                    this.isAuthenticatedSubject.next(true);
                    this.currentUserSubject.next(user);
                    this.updateUserRoles(user);
                } catch (e) {
                    this.logout();
                }
            }
        }
    }

    private updateUserRoles(user: User) {
        if (user.roles && user.roles.length > 0) {
            const role = user.roles.includes('ADMIN') ? 'ADMIN' : 'USER';
            this.userRoleSubject.next(role);
        }
    }

    login(credentials: any): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.authUrl}/login`, credentials).pipe(
            tap(response => {
                const user: User = {
                    id: response.userId.toString(),
                    username: response.email,
                    email: response.email,
                    roles: [response.role]
                };

                if (isPlatformBrowser(this.platformId)) {
                    localStorage.setItem('accessToken', response.token);
                    // Backend doesn't return refresh token yet
                    localStorage.setItem('user', JSON.stringify(user));
                }

                this.isAuthenticatedSubject.next(true);
                this.currentUserSubject.next(user);
                this.updateUserRoles(user);
            })
        );
    }

    register(user: any): Observable<any> {
        return this.http.post(`${this.authUrl}/register`, user);
    }

    logout(): void {
        if (isPlatformBrowser(this.platformId)) {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
                this.http.post(`${this.authUrl}/logout`, { refreshToken }).subscribe({
                    next: () => this.clearLocalSession(),
                    error: () => this.clearLocalSession()
                });
            } else {
                this.clearLocalSession();
            }
        } else {
            this.clearLocalSession();
        }
    }

    private clearLocalSession(): void {
        if (isPlatformBrowser(this.platformId)) {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
        }
        this.isAuthenticatedSubject.next(false);
        this.userRoleSubject.next(null);
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
    }

    getProfile(): Observable<User> {
        const user = this.currentUserSubject.value;
        if (user && user.id) {
            return this.http.get<any>(`${environment.apiUrl}/MS-CLIENT/api/v1/users/${user.id}`).pipe(
                map(response => {
                    return {
                        id: response.id.toString(),
                        username: response.email,
                        email: response.email,
                        firstName: response.firstName,
                        lastName: response.lastName,
                        roles: [response.role]
                    } as User;
                })
            );
        }
        return throwError(() => new Error('User ID not found'));
    }

    isLoggedIn(): boolean {
        return this.isAuthenticatedSubject.value;
    }

    isAdmin(): boolean {
        const role = this.userRoleSubject.value;
        return role === 'ADMIN';
    }

    getToken(): string | null {
        if (isPlatformBrowser(this.platformId)) {
            return localStorage.getItem('accessToken');
        }
        return null;
    }

    getAuthenticatedUser(): Observable<User> {
        return this.getProfile();
    }
}
