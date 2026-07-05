import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiResponse, PayOrderResponse } from '../schemas/order.schema';

type PaymentMethod = 'VIETQR' | 'PAYPAL';
type PaymentFlowType = 'QR' | 'REDIRECT';

interface PaymentInitiationResponse {
  method: PaymentMethod;
  flowType: PaymentFlowType;
  redirectUrl?: string;
  qrCode?: any;
}

interface PaymentCompletionResponse {
  method: PaymentMethod;
  order: PayOrderResponse;
  providerVerdict?: string;
}

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private readonly baseUrl = 'http://localhost:8080/oms/api/orders';

  constructor(private http: HttpClient) {}

  /** POST /oms/api/orders/{orderId}/payments/vietqr — generate QR code */
  generateVietQR(orderId: number): Observable<any> {
    return this.http
      .post<ApiResponse<PaymentInitiationResponse>>(`${this.baseUrl}/${orderId}/payments`, {
        method: 'VIETQR',
      })
      .pipe(
        map((res) => ({
          ...res,
          result: res.result.qrCode,
        })),
      );
  }

  /**
   * POST /oms/api/orders/{orderId}/payments/vietqr/simulate-callback
   * Simulates receiving a VietQR callback (replaces the manual Thunder Client step).
   * The backend triggers VietQR's Test Callback; VietQR then calls our transaction-sync,
   * which records the payment. The amount is read server-side from the order's invoice,
   * so no amount is sent from here. The response carries the finalized order and the
   * raw VietQR verdict.
   */
  simulateVietQRPayment(orderId: number): Observable<any> {
    return this.http
      .post<ApiResponse<PaymentCompletionResponse>>(
        `${this.baseUrl}/${orderId}/payments/VIETQR/complete`,
        {},
      )
      .pipe(
        map((res) => ({
          ...res,
          result: {
            order: res.result.order,
            vietQrVerdict: res.result.providerVerdict,
          },
        })),
      );
  }

  /** POST /oms/api/orders/{orderId}/payments/paypal — create PayPal order, returns redirect URL */
  createPayPalPayment(orderId: number): Observable<any> {
    return this.http
      .post<ApiResponse<PaymentInitiationResponse>>(`${this.baseUrl}/${orderId}/payments`, {
        method: 'PAYPAL',
      })
      .pipe(
        map((res) => ({
          ...res,
          result: {
            url: res.result.redirectUrl,
          },
        })),
      );
  }

  /** POST /oms/api/orders/{orderId}/payments/paypal/capture */
  capturePayPalPayment(orderId: number, token: string): Observable<ApiResponse<PayOrderResponse>> {
    return this.http
      .post<ApiResponse<PaymentCompletionResponse>>(
        `${this.baseUrl}/${orderId}/payments/PAYPAL/complete`,
        { token },
      )
      .pipe(
        map((res) => ({
          ...res,
          result: res.result.order,
        })),
      );
  }

  /** POST /oms/api/orders/{orderId}/finalize-payment */
  finalizePayment(orderId: number): Observable<ApiResponse<PayOrderResponse>> {
    return this.http.post<ApiResponse<PayOrderResponse>>(
      `${this.baseUrl}/${orderId}/finalize-payment`,
      {},
    );
  }
}
