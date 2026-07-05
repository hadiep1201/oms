import { Injectable } from '@angular/core';
import { PayOrderResponse } from '../schemas/order.schema';

@Injectable({
  providedIn: 'root'
})
export class CheckoutSessionService {
  private static readonly PENDING_ORDER_ID_KEY = 'aims_pending_order_id';
  private static readonly PENDING_PAYMENT_STATE_KEY = 'aims_pending_payment_state';
  private static readonly LAST_COMPLETED_ORDER_KEY = 'aims_last_completed_order';

  getPendingOrderId(): number {
    return Number(sessionStorage.getItem(CheckoutSessionService.PENDING_ORDER_ID_KEY) ?? 0);
  }

  setPendingOrderId(orderId: number): void {
    sessionStorage.setItem(CheckoutSessionService.PENDING_ORDER_ID_KEY, String(orderId));
  }

  clearPendingOrderId(): void {
    sessionStorage.removeItem(CheckoutSessionService.PENDING_ORDER_ID_KEY);
  }

  getPendingPaymentState<T>(): T | null {
    const raw = sessionStorage.getItem(CheckoutSessionService.PENDING_PAYMENT_STATE_KEY);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as T;
    } catch {
      this.clearPendingPaymentState();
      return null;
    }
  }

  setPendingPaymentState(state: unknown): void {
    sessionStorage.setItem(
      CheckoutSessionService.PENDING_PAYMENT_STATE_KEY,
      JSON.stringify(state)
    );
  }

  clearPendingPaymentState(): void {
    sessionStorage.removeItem(CheckoutSessionService.PENDING_PAYMENT_STATE_KEY);
  }

  getCompletedOrder(): PayOrderResponse | null {
    const raw = sessionStorage.getItem(CheckoutSessionService.LAST_COMPLETED_ORDER_KEY);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as PayOrderResponse;
    } catch {
      this.clearCompletedOrder();
      return null;
    }
  }

  setCompletedOrder(order: PayOrderResponse): void {
    sessionStorage.setItem(
      CheckoutSessionService.LAST_COMPLETED_ORDER_KEY,
      JSON.stringify(order)
    );
  }

  clearCompletedOrder(): void {
    sessionStorage.removeItem(CheckoutSessionService.LAST_COMPLETED_ORDER_KEY);
  }
}
