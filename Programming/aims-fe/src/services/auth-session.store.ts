import { Injectable, signal } from '@angular/core';
import { AuthSession, LoginResponse } from '../schemas/auth.schema';

const ACCESS_TOKEN_KEY = 'aims_access_token';
const REFRESH_TOKEN_KEY = 'aims_refresh_token';
const SESSION_KEY = 'aims_auth_session';

/**
 * SRP: client-side auth session persistence only.
 * Coupling: common coupling with sessionStorage.
 * Cohesion: functional cohesion.
 */
@Injectable({ providedIn: 'root' })
export class AuthSessionStore {
  session = signal<AuthSession | null>(this.loadSession());

  persist(result: LoginResponse): void {
    const session: AuthSession = {
      accessToken: result.accessToken,
      refreshToken: result.refreshToken,
      userId: result.userId,
      userName: result.userName,
      email: result.email,
      roles: result.roles ?? [],
    };
    sessionStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  clear(): void {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem('isLoggedIn');
    this.session.set(null);
  }

  private loadSession(): AuthSession | null {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      return null;
    }
  }
}
