import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse, LoginRequest, LoginResponse } from '../schemas/auth.schema';

/**
 * SRP: auth HTTP API calls only.
 * Coupling: data coupling with AuthController via HttpClient.
 * Cohesion: functional cohesion.
 */
@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private http = inject(HttpClient);
  private readonly authApiUrl = 'http://localhost:8080/oms/api/auth';

  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.authApiUrl}/login`, request);
  }

  refreshToken(refreshToken: string): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.authApiUrl}/refresh`, { refreshToken });
  }

  logout(refreshToken: string): Observable<unknown> {
    return this.http.post(`${this.authApiUrl}/logout`, { refreshToken });
  }
}
