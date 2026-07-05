import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserResponse {
  userId: number;
  userName: string;
  email: string;
  avatarUrl: string;
  status: string | null;
}

export interface CreateUserRequest {
  userName: string;
  email: string;
  password?: string; // Optional because backend handles initial if needed
  avatarUrl?: string;
}

export interface UpdateUserRequest {
  userName: string;
  email: string;
  avatarUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'http://localhost:8080/aims/api/admin/users';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
  }

  createUser(user: CreateUserRequest): Observable<UserResponse> {
    // Adding a dummy password if none provided, since backend requires it
    if (!user.password) {
        user.password = 'DefaultPass@123';
    }
    return this.http.post<UserResponse>(this.apiUrl, user);
  }

  updateUser(id: number, user: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, user);
  }

  deactivateUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
