export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  userName: string;
  email: string;
  roles: string[];
}

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  userId: number;
  userName: string;
  email: string;
  roles: string[];
}

export interface ApiResponse<T> {
  code: number;
  message?: string;
  result: T;
}
