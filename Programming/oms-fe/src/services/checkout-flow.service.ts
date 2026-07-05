import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of, throwError } from 'rxjs';
import {
  ApiResponse,
  CartItem,
  DeliveryInfoRequest,
  OrderItemRequest,
  PlaceOrderRequest,
  PlaceOrderResponse,
  ShippingFeeRequest,
  ShippingFeeResponse,
  StockValidationResponse
} from '../schemas/order.schema';
import { OrderService } from './order.service';
import { CheckoutSessionService } from './checkout-session.service';
import { CheckoutErrorPolicyService } from './checkout-error-policy.service';

export interface PaymentNavigationState {
  orderId: number;
  deliveryInfo: DeliveryInfoRequest;
  orderItems: Array<{
    productId: number;
    title: string;
    price: number;
    quantity: number;
    imageUrl?: string;
  }>;
  subtotal: number;
  vat: number;
  shippingFee: number;
  total: number;
  totalWeight: number;
}

export interface DeliverySubmissionContext {
  cartItems: CartItem[];
  deliveryInfo: DeliveryInfoRequest;
  totalWeight: number;
}

export interface CheckoutResult {
  errorMessage?: string;
}

export interface CartValidationResult extends CheckoutResult {
  canProceed: boolean;
}

export interface CheckoutSubmitResult {
  paymentState: PaymentNavigationState;
}

@Injectable({
  providedIn: 'root'
})
export class CheckoutFlowService {
  private readonly orderService = inject(OrderService);
  private readonly checkoutSessionService = inject(CheckoutSessionService);
  private readonly checkoutErrorPolicyService = inject(CheckoutErrorPolicyService);

  validateCart(cartItems: CartItem[]): Observable<CartValidationResult> {
    if (cartItems.length === 0) {
      return of({ canProceed: false, errorMessage: 'Cart is empty!' });
    }

    const items = cartItems.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }));
    const productTitleById = new Map(
      cartItems.map(item => [item.productId, item.product.title])
    );

    return this.orderService.validateStock(items).pipe(
      map((res: ApiResponse<StockValidationResponse>) => {
        if (!res.result.valid) {
          return {
            canProceed: false,
            errorMessage: 'Some items are out of stock: ' +
              res.result.unavailableItems
                .map(i => `${i.productTitle ?? productTitleById.get(i.productId) ?? `product #${i.productId}`} (available quantity ${i.availableQuantity})`)
                .join(', ')
          };
        }

        return { canProceed: true };
      }),
      catchError(() => of({
        canProceed: false,
        errorMessage: 'Error validating stock. Please make sure backend is running.'
      }))
    );
  }

  calculateShippingFee(request: ShippingFeeRequest): Observable<ApiResponse<ShippingFeeResponse>> {
    return this.orderService.calculateShippingFee(request);
  }

  submitOrder(
    request: PlaceOrderRequest,
    context: DeliverySubmissionContext
  ): Observable<CheckoutSubmitResult> {
    const pendingOrderId = this.checkoutSessionService.getPendingOrderId();

    const request$ = pendingOrderId > 0
      ? this.orderService.recalculateOrder(pendingOrderId, request).pipe(
          catchError((err) => {
            if (this.checkoutErrorPolicyService.shouldFallbackToPlaceOrder(err)) {
              this.checkoutSessionService.clearPendingOrderId();
              return this.orderService.placeOrder(request).pipe(
                catchError((placeOrderErr) => this.toGenericSubmitError(placeOrderErr))
              );
            }

            if (this.checkoutErrorPolicyService.shouldBlockOrderEditing(err)) {
              return throwError(() => new Error(this.checkoutErrorPolicyService.getNonDraftOrderMessage()));
            }

            return this.toGenericSubmitError(err);
          })
        )
      : this.orderService.placeOrder(request).pipe(
          catchError((err) => this.toGenericSubmitError(err))
        );

    return request$.pipe(
      map((res) => ({
        paymentState: this.buildPaymentState(res, context)
      }))
    );
  }

  private buildPaymentState(
    res: ApiResponse<PlaceOrderResponse>,
    context: DeliverySubmissionContext
  ): PaymentNavigationState {
    const imageUrlByProductId = new Map(
      context.cartItems.map(item => [item.productId, item.product.imageUrl])
    );

    return {
      orderId: res.result.orderId,
      deliveryInfo: context.deliveryInfo,
      orderItems: res.result.orderItems.map(item => ({
        productId: item.productId,
        title: item.productTitle,
        price: item.price,
        quantity: item.quantity,
        imageUrl: imageUrlByProductId.get(item.productId)
      })),
      subtotal: res.result.subTotal,
      vat: res.result.vatAmount,
      shippingFee: res.result.shippingFee,
      total: res.result.totalAmount,
      totalWeight: context.totalWeight
    };
  }

  private toGenericSubmitError(_: unknown): Observable<never> {
    return throwError(() => new Error(this.checkoutErrorPolicyService.getGenericSubmitErrorMessage()));
  }
}
