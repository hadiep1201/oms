import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CheckoutErrorPolicyService {
  private static readonly RECALCULATE_ERROR_CODES = {
    ORDER_NOT_FOUND: 4100,
    ORDER_EXPIRED: 4101,
    ORDER_ALREADY_PAID: 4102,
    INVOICE_NOT_FOUND: 4103,
    INVOICE_VOID: 4104,
    INVOICE_FINALIZED: 4105
  } as const;

  shouldFallbackToPlaceOrder(err: unknown): boolean {
    const errorCode = this.getErrorCode(err);
    const message = this.getMessage(err);

    return errorCode === CheckoutErrorPolicyService.RECALCULATE_ERROR_CODES.ORDER_NOT_FOUND
      || errorCode === CheckoutErrorPolicyService.RECALCULATE_ERROR_CODES.INVOICE_NOT_FOUND
      || errorCode === CheckoutErrorPolicyService.RECALCULATE_ERROR_CODES.ORDER_EXPIRED
      || errorCode === CheckoutErrorPolicyService.RECALCULATE_ERROR_CODES.INVOICE_VOID
      || message.includes('order not found')
      || message.includes('invoice not found')
      || message.includes('expired')
      || message.includes('void')
      || message.includes('no longer payable');
  }

  shouldBlockOrderEditing(err: unknown): boolean {
    const errorCode = this.getErrorCode(err);
    const message = this.getMessage(err);

    return errorCode === CheckoutErrorPolicyService.RECALCULATE_ERROR_CODES.ORDER_ALREADY_PAID
      || errorCode === CheckoutErrorPolicyService.RECALCULATE_ERROR_CODES.INVOICE_FINALIZED
      || message.includes('only draft invoice can be recalculated')
      || message.includes('only pending_payment orders can be recalculated');
  }

  getNonDraftOrderMessage(): string {
    return 'This order has already reached the payment-confirmed stage, so it can no longer be edited here.';
  }

  getGenericSubmitErrorMessage(): string {
    return 'Error creating order. Please try again later.';
  }

  private getErrorCode(err: any): number | undefined {
    const code = err?.error?.code;
    return typeof code === 'number' ? code : undefined;
  }

  private getMessage(err: any): string {
    return String(err?.error?.message ?? err?.message ?? '').toLowerCase();
  }
}
