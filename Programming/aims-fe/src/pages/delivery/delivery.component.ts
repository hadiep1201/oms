import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CartService } from '../../services/cart.service';
import { CheckoutFlowService } from '../../services/checkout-flow.service';
import { CheckoutNavigationService } from '../../services/checkout-navigation.service';
import { PlaceOrderRequest } from '../../schemas/order.schema';

@Component({
  selector: 'app-delivery',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CurrencyPipe],
  templateUrl: './delivery.component.html',
  styleUrls: ['./delivery.component.css'],})
export class DeliveryComponent implements OnInit {
  cartService = inject(CartService);
  checkoutFlowService = inject(CheckoutFlowService);
  checkoutNavigationService = inject(CheckoutNavigationService);
  fb = inject(FormBuilder);
  router = inject(Router);

  shippingFee = 0;
  isSubmitting = false;
  hasAttemptedSubmit = false;
  errorMessage = '';
  toastMessage = '';
  toastType: 'error' | 'success' = 'success';

  deliveryForm: FormGroup = this.fb.group({
    receiverName: ['', Validators.required],
    phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10,11}$')]],
    email: ['', [Validators.required, Validators.email]],
    address: ['', Validators.required],
    city: ['', Validators.required],
    instruction: [''],
    expectedDate: ['']
  });

  ngOnInit() {
    if (this.cartService.items().length === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state ?? history.state;
    if (state?.['deliveryInfo']) {
      this.deliveryForm.patchValue(state['deliveryInfo']);
    }

    // Subscribe to location changes to calculate shipping fee
    this.deliveryForm.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged((a, b) => 
          a.address === b.address && a.city === b.city
        )
      )
      .subscribe(val => {
        if (val.address && val.city) {
          this.calculateFee(val.address, val.city);
        }
      });
  }

  calculateFee(address: string, city: string) {
    const items = this.cartService.items().map(i => ({
      productId: i.productId,
      quantity: i.quantity
    }));

    this.checkoutFlowService.calculateShippingFee({ items, address, city }).subscribe({
      next: (res) => {
        const oldFee = this.shippingFee;
        this.shippingFee = res.result.shippingFee;
        if (oldFee !== this.shippingFee && this.deliveryForm.valid) {
          this.showToast('Phí giao hàng và tổng số tiền đã được tính lại thành công.', 'success');
        }
      },
      error: (err) => {
        console.error('Lỗi tính phí ship', err);
        this.showToast('Không thể tính toán lại phí giao hàng.', 'error');
      }
    });
  }

  continueToPayment() {
    this.hasAttemptedSubmit = true;

    if (this.deliveryForm.invalid) {
      this.errorMessage = 'Thông tin giao hàng không hợp lệ. Vui lòng cập nhật các trường bắt buộc và thử lại.';
      this.showToast(this.errorMessage, 'error');
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    const cartItems = this.cartService.items();
    const items = cartItems.map(i => ({
      productId: i.productId,
      quantity: i.quantity
    }));

    const req: PlaceOrderRequest = {
      items,
      deliveryInfo: this.deliveryForm.value
    };

    this.checkoutFlowService.submitOrder(req, {
      cartItems,
      deliveryInfo: this.deliveryForm.value,
      totalWeight: this.cartService.totalWeight()
    }).subscribe({
      next: (result) => {
        this.isSubmitting = false;
        this.checkoutNavigationService.toPayment(result.paymentState);
      },
      error: (err: Error) => {
        this.errorMessage = err.message;
        this.isSubmitting = false;
        this.showToast(this.errorMessage, 'error');
      }
    });
  }

  shouldShowFieldError(controlName: string): boolean {
    const control = this.deliveryForm.get(controlName);
    return !!control && control.invalid && (control.touched || this.hasAttemptedSubmit);
  }

  private showToast(message: string, type: 'error' | 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = '';
    }, 3500);
  }
}

