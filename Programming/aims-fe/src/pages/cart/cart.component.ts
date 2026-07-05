import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { CartService } from '../../services/cart.service';
import { CheckoutFlowService } from '../../services/checkout-flow.service';
import { CheckoutNavigationService } from '../../services/checkout-navigation.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css'],})
export class CartComponent {
  cartService = inject(CartService);
  checkoutFlowService = inject(CheckoutFlowService);
  checkoutNavigationService = inject(CheckoutNavigationService);
  private cdr = inject(ChangeDetectorRef);

  toastMessage = '';
  toastType: 'error' = 'error';
  isCheckingOut = false;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;
  private quantityDrafts = new Map<number, string>();

  checkout() {
    if (this.isCheckingOut) {
      return;
    }

    this.isCheckingOut = true;
    const items = this.cartService.items();

    this.checkoutFlowService.validateCart(items).subscribe({
      next: (result) => {
        this.isCheckingOut = false;
        if (result.canProceed) {
          this.checkoutNavigationService.toDeliveryFromCart();
          return;
        }

        this.showToast(result.errorMessage ?? '', 'error');
      },
      error: () => {
        this.isCheckingOut = false;
        this.showToast('Error validating stock. Please try again.', 'error');
      }
    });
  }

  changeQuantity(productId: number, quantity: number): void {
    this.quantityDrafts.delete(productId);
    const result = this.cartService.setQuantity(productId, quantity);
    if (!result.ok) {
      this.showToast(result.message ?? 'Cannot update quantity right now.', 'error');
    }
  }

  onQuantityInput(productId: number, rawValue: string): void {
    this.quantityDrafts.set(productId, rawValue);
  }

  commitQuantityInput(productId: number, currentQuantity: number): void {
    const draft = this.quantityDrafts.get(productId);
    if (draft === undefined) {
      return;
    }

    const trimmed = draft.trim();
    if (!trimmed) {
      this.quantityDrafts.delete(productId);
      this.cdr.detectChanges();
      return;
    }

    const parsed = Number(trimmed);
    const quantity = Number.isFinite(parsed) ? Math.floor(parsed) : currentQuantity;
    this.changeQuantity(productId, Math.max(1, quantity));
  }

  getQuantityDraft(productId: number, quantity: number): string | number {
    return this.quantityDrafts.get(productId) ?? quantity;
  }

  trackByProductId(_: number, item: { productId: number }): number {
    return item.productId;
  }

  private showToast(message: string, type: 'error'): void {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = null;
    }

    this.toastMessage = '';
    this.toastType = type;
    this.cdr.detectChanges();

    this.toastMessage = message;
    this.cdr.detectChanges();
    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
      this.toastTimer = null;
      this.cdr.detectChanges();
    }, 3500);
  }
}
