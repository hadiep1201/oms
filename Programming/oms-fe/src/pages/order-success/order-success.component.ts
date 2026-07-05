import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PayOrderResponse } from '../../schemas/order.schema';
import { CartService } from '../../services/cart.service';
import { CheckoutSessionService } from '../../services/checkout-session.service';

@Component({
  selector: 'app-order-success',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './order-success.component.html',
  styleUrls: ['./order-success.component.css'],})
export class OrderSuccessComponent implements OnInit {
  order: PayOrderResponse | null = null;

  constructor(
    private router: Router,
    private cartService: CartService,
    private checkoutSessionService: CheckoutSessionService
  ) {}

  ngOnInit(): void {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state ?? history.state;
    this.order = state?.['order'] ?? this.checkoutSessionService.getCompletedOrder();

    if (this.order) {
      this.cartService.clearCart();
      this.checkoutSessionService.clearPendingOrderId();
      this.checkoutSessionService.clearPendingPaymentState();
      this.checkoutSessionService.setCompletedOrder(this.order);
    }
  }

  continueShopping(): void {
    this.checkoutSessionService.clearCompletedOrder();
    this.router.navigate(['/cart']);
  }

  formatVND(amount: number): string {
    return amount.toLocaleString('vi-VN') + 'đ';
  }
}
