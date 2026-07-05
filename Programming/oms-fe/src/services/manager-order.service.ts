import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import {
  ApiResponse,
  ApproveOrderRequest,
  ManagerOrderPageResponse,
  ManagerOrderResponse,
  RejectOrderRequest,
} from '../schemas/order.schema';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ManagerOrderService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private readonly apiUrl = 'http://localhost:8080/oms/api/manager/orders';

  getPendingOrders(page = 0, size = 30): Observable<ApiResponse<ManagerOrderPageResponse>> {
    return this.http.get<ApiResponse<ManagerOrderPageResponse>>(
      `${this.apiUrl}?page=${page}&size=${size}`
    );
  }

  getOrder(orderId: number): Observable<ApiResponse<ManagerOrderResponse>> {
    return this.http.get<ApiResponse<ManagerOrderResponse>>(`${this.apiUrl}/${orderId}`);
  }

  approveOrder(orderId: number): Observable<ApiResponse<ManagerOrderResponse>> {
    const managerUserId = this.authService.getUserId();
    if (managerUserId === null) {
      return throwError(() => new Error('Manager session is missing.'));
    }

    const request: ApproveOrderRequest = { managerUserId };
    return this.http.post<ApiResponse<ManagerOrderResponse>>(
      `${this.apiUrl}/${orderId}/approve`,
      request
    );
  }

  rejectOrder(orderId: number, reason?: string): Observable<ApiResponse<ManagerOrderResponse>> {
    const managerUserId = this.authService.getUserId();
    if (managerUserId === null) {
      return throwError(() => new Error('Manager session is missing.'));
    }

    const request: RejectOrderRequest = {
      managerUserId,
      reason: reason?.trim() || undefined,
    };
    return this.http.post<ApiResponse<ManagerOrderResponse>>(
      `${this.apiUrl}/${orderId}/reject`,
      request
    );
  }
}
