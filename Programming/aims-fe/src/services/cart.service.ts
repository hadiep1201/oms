import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApiResponse, CartItem, Product, StockValidationResponse } from '../schemas/order.schema';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private http = inject(HttpClient);
  private readonly orderApiUrl = 'http://localhost:8080/aims/api/orders';
  private cartItemsSignal = signal<CartItem[]>([]);

  readonly items = this.cartItemsSignal.asReadonly();

  readonly subTotal = computed(() => {
    return this.items().reduce((sum, item) => sum + (item.product.currentPrice * item.quantity), 0);
  });

  readonly vatAmount = computed(() => {
    return this.subTotal() * 0.1;
  });

  readonly totalWeight = computed(() => {
    return this.items().reduce((sum, item) => sum + (item.product.weight * item.quantity), 0);
  });

  constructor() {
    this.loadCart();
  }

  private loadCart() {
    const saved = localStorage.getItem('aims_cart');
    if (saved) {
      try {
        this.cartItemsSignal.set(JSON.parse(saved));
      } catch (e) {
        console.error('Failed to parse cart from local storage', e);
      }
    }
  }

  private saveCart(items: CartItem[]) {
    localStorage.setItem('aims_cart', JSON.stringify(items));
    this.cartItemsSignal.set(items);
  }

  async addDetailToCart(detail: any, quantity: number = 1): Promise<{ ok: boolean; message?: string }> {
    const fullProduct: Product = {
      id: detail.id,
      title: detail.title,
      category: detail.category,
      generalDescription: detail.generalDescription || '',
      barcode: detail.barcode || '',
      imageUrl: detail.imageUrl || '',
      originalValue: detail.originalValue || detail.currentPrice,
      currentPrice: detail.currentPrice,
      weight: detail.weight || 0.5,
      length: detail.length || 15
    };
    return this.addToCart(fullProduct, quantity);
  }

  async addToCart(product: Product, quantity: number = 1): Promise<{ ok: boolean; message?: string }> {
    if (quantity <= 0) {
      return { ok: false, message: 'Quantity must be greater than 0' };
    }

    const currentItems = this.items();
    const existing = currentItems.find(i => i.productId === product.id);
    const currentQtyInCart = existing ? existing.quantity : 0;
    const targetQtyInCart = currentQtyInCart + quantity;

    const canAdd = await this.validateStock(product.id, targetQtyInCart);
    if (!canAdd.ok) {
      return canAdd;
    }

    if (existing) {
      this.updateQuantity(product.id, existing.quantity + quantity);
    } else {
      const newItem: CartItem = {
        productId: product.id,
        quantity,
        product
      };
      this.saveCart([...currentItems, newItem]);
    }

    return { ok: true };
  }

  removeItem(productId: number) {
    const newItems = this.items().filter(i => i.productId !== productId);
    this.saveCart(newItems);
  }

  updateQuantity(productId: number, quantity: number) {
    if (quantity <= 0) {
      this.removeItem(productId);
      return;
    }
    const newItems = this.items().map(i => 
      i.productId === productId ? { ...i, quantity } : i
    );
    this.saveCart(newItems);
  }
  
  clearCart() {
    localStorage.removeItem('aims_cart');
    this.cartItemsSignal.set([]);
  }

  setQuantity(productId: number, quantity: number): { ok: boolean; message?: string } {
    if (quantity <= 0) {
      this.removeItem(productId);
      return { ok: true };
    }

    const existing = this.items().find(i => i.productId === productId);
    if (!existing) {
      return { ok: false, message: 'Item not found in cart.' };
    }

    this.updateQuantity(productId, quantity);
    return { ok: true };
  }

  private async validateStock(productId: number, targetQtyInCart: number): Promise<{ ok: boolean; message?: string }> {
    try {
      const response = await firstValueFrom(
        this.http.post<ApiResponse<StockValidationResponse>>(
          `${this.orderApiUrl}/validate-stock`,
          [{ productId, quantity: targetQtyInCart }]
        )
      );

      if (response?.result?.valid) {
        return { ok: true };
      }

      const unavailableItem = response?.result?.unavailableItems?.find((item) => item.productId === productId);
      const remaining = unavailableItem?.availableQuantity;
      const suffix = remaining !== undefined ? ` (remaining stock: ${remaining})` : '';
      return { ok: false, message: `Cannot add to cart: quantity exceeds stock${suffix}` };
    } catch {
      return { ok: false, message: 'Cannot validate stock right now. Please try again.' };
    }
  }
}
