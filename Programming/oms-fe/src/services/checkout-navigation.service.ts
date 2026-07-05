import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { CheckoutSessionService } from './checkout-session.service';
import { PaymentNavigationState } from './checkout-flow.service';

@Injectable({
  providedIn: 'root'
})
export class CheckoutNavigationService {
  constructor(
    private router: Router,
    private checkoutSessionService: CheckoutSessionService
  ) {}

  toDeliveryFromCart(): void {
    this.checkoutSessionService.clearPendingOrderId();
    this.checkoutSessionService.clearPendingPaymentState();
    this.router.navigate(['/delivery']);
  }

  toPayment(state: PaymentNavigationState): void {
    this.checkoutSessionService.setPendingOrderId(state.orderId);
    this.checkoutSessionService.setPendingPaymentState(state);
    this.router.navigate(['/payment'], { state });
  }
}
