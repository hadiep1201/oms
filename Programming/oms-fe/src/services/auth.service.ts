import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiResponse, LoginRequest, LoginResponse } from '../schemas/auth.schema';
import { AuthApiService } from './auth-api.service';
import { AuthSessionStore } from './auth-session.store';

/**
 * Facade for authentication session, role helpers, and auth API orchestration.
 * Coupling: data coupling with AuthApiService and AuthSessionStore.
 * Cohesion: functional cohesion — authentication use cases for the frontend.
 *
 * SOLID Review:
 * - SRP: compliant — HTTP and persistence delegated; this service orchestrates auth flows and roles.
 * - OCP: new role rules can be added without changing API/session stores.
 * - DIP: depends on injected auth collaborators instead of HttpClient/sessionStorage directly.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private authApi = inject(AuthApiService);
  private sessionStore = inject(AuthSessionStore);
  private router = inject(Router);

  session = this.sessionStore.session;

  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.authApi
      .login(request)
      .pipe(tap((res) => this.sessionStore.persist(res.result)));
  }

  refreshToken(): Observable<ApiResponse<LoginResponse>> {
    const refreshToken = this.sessionStore.getRefreshToken();
    return this.authApi
      .refreshToken(refreshToken ?? '')
      .pipe(tap((res) => this.sessionStore.persist(res.result)));
  }

  logout(): void {
    const refreshToken = this.sessionStore.getRefreshToken();
    if (refreshToken) {
      this.authApi.logout(refreshToken).subscribe({
        error: () => undefined,
      });
    }
    this.clearSession();
    this.router.navigate(['/login']);
  }

  clearLocalSession(): void {
    this.clearSession();
  }

  getAccessToken(): string | null {
    return this.sessionStore.getAccessToken();
  }

  getUserId(): number | null {
    return this.session()?.userId ?? null;
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  hasAnyRole(roles: string[]): boolean {
    const userRoles = this.session()?.roles ?? [];
    return roles.some((role) => userRoles.includes(role));
  }

  canAccessManager(): boolean {
    return this.hasAnyRole(['PRODUCT_MANAGER']);
  }

  isAdminOnly(): boolean {
    const roles = this.session()?.roles ?? [];
    return roles.includes('ADMIN') && !roles.includes('PRODUCT_MANAGER');
  }

  canUseStorefrontCart(): boolean {
    return !(this.isAuthenticated() && this.canAccessManager());
  }

  private clearSession(): void {
    this.sessionStore.clear();
  }
}
