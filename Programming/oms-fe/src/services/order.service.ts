import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  OrderItemRequest,
  PlaceOrderRequest,
  PlaceOrderResponse,
  ShippingFeeRequest,
  ShippingFeeResponse,
  StockValidationResponse
} from '../schemas/order.schema';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  // Cập nhật base url với endpoint của backend.
  private apiUrl = 'http://localhost:8080/oms/api/orders';

  validateStock(items: OrderItemRequest[]): Observable<ApiResponse<StockValidationResponse>> {
    return this.http.post<ApiResponse<StockValidationResponse>>(`${this.apiUrl}/validate-stock`, items);
  }

  calculateShippingFee(request: ShippingFeeRequest): Observable<ApiResponse<ShippingFeeResponse>> {
    return this.http.post<ApiResponse<ShippingFeeResponse>>(`${this.apiUrl}/shipping-fee`, request);
  }

  placeOrder(request: PlaceOrderRequest): Observable<ApiResponse<PlaceOrderResponse>> {
    return this.http.post<ApiResponse<PlaceOrderResponse>>(`${this.apiUrl}/place`, request);
  }

  recalculateOrder(orderId: number, request: PlaceOrderRequest): Observable<ApiResponse<PlaceOrderResponse>> {
    return this.http.put<ApiResponse<PlaceOrderResponse>>(`${this.apiUrl}/${orderId}/recalculate`, request);
  }
}
