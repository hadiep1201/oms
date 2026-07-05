import {
  Component,
  OnInit,
  OnDestroy,
  ElementRef,
  ViewChild,
  ChangeDetectorRef,
} from '@angular/core';
import QRCode from 'qrcode';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { CartService } from '../../services/cart.service';
import { CheckoutSessionService } from '../../services/checkout-session.service';
import type { PaymentNavigationState } from '../../services/checkout-flow.service';

/**
 * Payment page component (step 2 of 2: Delivery → Payment).
 *
 * Expects navigation state from DeliveryComponent:
 *   router.navigate(['/payment'], {
 *     state: {
 *       orderId, deliveryInfo, orderItems,
 *       subtotal, vat, shippingFee, total, totalWeight
 *     }
 *   });
 *
 * Matches mockup: PAY ORDER 2 (Figma)
 */
@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-wrapper">
      <div
        *ngIf="toastMessage"
        class="toast"
        [class.error]="toastType === 'error'"
        [class.success]="toastType === 'success'"
      >
        {{ toastMessage }}
      </div>
      <!-- ── Step indicator ── -->
      <div class="steps-bar">
        <div class="step done">
          <span class="step-circle">1</span>
          <span class="step-label">GIAO HÀNG</span>
        </div>
        <div class="step-connector"></div>
        <div class="step active">
          <span class="step-circle">2</span>
          <span class="step-label">THANH TOÁN</span>
        </div>
      </div>

      <!-- ── Main layout ── -->
      <div class="payment-layout">
        <!-- ────── LEFT PANEL ────── -->
        <div class="left-panel">
          <!-- Header -->
          <div class="panel-header">
            <h2 class="panel-title">THANH TOÁN</h2>
            <button class="back-btn" (click)="goBack()">quay lại...</button>
          </div>

          <!-- Shipping summary card -->
          <div class="info-card">
            <div class="info-row">
              <svg
                class="info-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#8E8E8E"
                stroke-width="2"
                width="22"
                height="22"
              >
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" />
                <circle cx="12" cy="9" r="2.5" />
              </svg>
              <div>
                <p class="info-label">GIAO HÀNG ĐẾN</p>
                <p class="info-value">{{ deliveryInfo?.address || '—' }}</p>
                <p class="info-sub">{{ deliveryInfo?.city }}</p>
              </div>
            </div>
            <div class="info-divider"></div>
            <div class="info-row">
              <svg
                class="info-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#8E8E8E"
                stroke-width="1.6"
                width="26"
                height="26"
              >
                <rect x="1" y="3" width="15" height="13" rx="1" />
                <path d="M16 8h4l3 4v5h-7V8z" />
                <circle cx="5.5" cy="18.5" r="2.5" />
                <circle cx="18.5" cy="18.5" r="2.5" />
              </svg>
              <div>
                <p class="info-label">PHƯƠNG THỨC GIAO HÀNG</p>
                <p class="info-value">Giao hàng tiêu chuẩn</p>
                <p class="info-sub">Thời gian dự kiến: 2-3 ngày</p>
              </div>
            </div>
          </div>

          <!-- Payment method selector -->
          <div class="method-list">
            <div
              class="method-card vietqr-card"
              [class.selected]="selectedMethod === 'vietqr'"
              (click)="selectMethod('vietqr')"
            >
              <div class="method-icon vietqr-icon">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  width="32"
                  height="32"
                >
                  <rect x="3" y="3" width="7" height="7" rx="1" />
                  <rect x="14" y="3" width="7" height="7" rx="1" />
                  <rect x="3" y="14" width="7" height="7" rx="1" />
                  <path d="M14 14h3v3h-3z M17 17h3v3h-3z M14 20h3" />
                </svg>
              </div>
              <div>
                <p class="method-name">VIETQR (QUÉT MÃ THANH TOÁN)</p>
                <p class="method-desc">Thanh toán tức thì qua ứng dụng ngân hàng của bạn</p>
              </div>
            </div>

            <div
              class="method-card paypal-card"
              [class.selected]="selectedMethod === 'paypal'"
              (click)="selectMethod('paypal')"
            >
              <div class="method-icon">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="#1E1E1E"
                  stroke-width="2.5"
                  width="28"
                  height="28"
                >
                  <rect x="1" y="4" width="22" height="16" rx="3" />
                  <line x1="1" y1="10" x2="23" y2="10" />
                </svg>
              </div>
              <div>
                <p class="method-name">THẺ TÍN DỤNG (PAYPAL)</p>
                <p class="method-desc">Thanh toán bảo mật bằng thẻ tín dụng hoặc thẻ ghi nợ</p>
              </div>
            </div>
          </div>

          <!-- QR code area (visible only for VietQR) -->
          <div *ngIf="selectedMethod === 'vietqr'" class="qr-area">
            <div *ngIf="isLoadingQR" class="qr-loading">Đang tạo mã QR…</div>
            <div *ngIf="qrError" class="qr-error">{{ qrError }}</div>

            <ng-container *ngIf="qrCode && !isLoadingQR">
              <!-- QR image -->
              <div class="qr-card">
                <div class="vietqr-brand">
                  <span class="brand-v">V</span><span class="brand-rest">IETQR</span>
                </div>
                <canvas #qrCanvas class="qr-img"></canvas>
                <div class="qr-footer">
                  <span>napas 247</span>
                  <span class="qr-bank">VietinBank</span>
                </div>
              </div>

              <!-- Countdown timer (right of QR) -->
              <div class="timer-block">
                <p class="timer-label">Thời gian thanh toán còn lại</p>
                <p class="timer-value" [class.urgent]="timeLeft < 60">
                  {{ formatTime(timeLeft) }}
                </p>
              </div>
            </ng-container>
          </div>

          <button
            *ngIf="selectedMethod === 'vietqr'"
            class="purchase-btn"
            (click)="confirmVietQRPayment()"
            [disabled]="isProcessing || !qrCode"
          >
            {{ isProcessing ? 'ĐANG XỬ LÝ…' : 'TÔI ĐÃ THANH TOÁN' }}
          </button>
          <button
            *ngIf="selectedMethod === 'paypal'"
            class="purchase-btn"
            (click)="redirectToPayPal()"
            [disabled]="isProcessing"
          >
            {{ isProcessing ? 'ĐANG CHUYỂN HƯỚNG…' : 'TIẾP TỤC VỚI PAYPAL' }}
          </button>
        </div>

        <!-- ────── RIGHT PANEL: Order Summary ────── -->
        <div class="right-panel">
          <div class="summary-card">
            <p class="summary-title">TÓM TẮT ĐƠN HÀNG</p>
            <div class="summary-divider"></div>

            <div class="summary-items">
              <div *ngFor="let item of orderItems" class="summary-item">
                <img [src]="item.imageUrl || 'assets/placeholder.png'" alt="" class="item-thumb" />
                <div class="item-meta">
                  <p class="item-name">{{ item.title }}</p>
                  <p class="item-qty">SL: {{ item.quantity }} &times; {{ formatVND(item.price) }}</p>
                </div>
                <p class="item-price">{{ formatVND(item.price * item.quantity) }}</p>
              </div>
            </div>

            <div class="summary-divider light"></div>

            <div class="summary-row">
              <span>Tổng khối lượng</span>
              <span><b>{{ totalWeight }}kg</b></span>
            </div>
            <div class="summary-row">
              <span>Tạm tính (chưa VAT)</span>
              <span><b>{{ formatVND(subtotal) }}</b></span>
            </div>
            <div class="summary-row">
              <span>Thuế VAT (10%)</span>
              <span><b>{{ formatVND(vat) }}</b></span>
            </div>
            <div class="summary-row">
              <span>Tổng tiền sản phẩm (gồm VAT)</span>
              <span><b>{{ formatVND(subtotal + vat) }}</b></span>
            </div>
            <div class="summary-row">
              <span>Phí giao hàng</span>
              <span><b>{{ shippingFee === 0 ? '0đ' : formatVND(shippingFee) }}</b></span>
            </div>

            <div class="summary-divider light"></div>

            <div class="summary-row total-row">
              <strong>TỔNG CỘNG</strong>
              <strong>{{ formatVND(total) }}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      /* ── Page wrapper ── */
      .page-wrapper {
        background: #f3f2f1;
        min-height: 100vh;
        font-family: 'Inter', sans-serif;
      }

      /* ── Step bar ── */
      .steps-bar {
        display: flex;
        align-items: center;
        padding: 28px 40px 0;
        gap: 0;
      }
      .step {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .step-circle {
        width: 31px;
        height: 31px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #d9d9d9;
        color: #000;
        font-size: 15px;
        font-weight: 700;
      }
      .step-label {
        font-size: 13px;
        font-weight: 700;
        color: #000;
        letter-spacing: 0.5px;
      }
      .step-connector {
        width: 224px;
        height: 1px;
        background: #d4d2d2;
        margin: 0 16px;
      }

      /* ── Main layout ── */
      .payment-layout {
        display: flex;
        gap: 32px;
        padding: 28px 40px 60px;
        max-width: 960px;
        margin: 0 auto;
      }
      .left-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 18px;
      }
      .right-panel {
        width: 440px;
        flex-shrink: 0;
      }

      /* ── Panel header ── */
      .panel-header {
        display: flex;
        align-items: baseline;
        justify-content: space-between;
      }
      .panel-title {
        font-size: 30px;
        font-weight: 700;
        margin: 0;
        color: #000;
      }
      .back-btn {
        background: none;
        border: none;
        cursor: pointer;
        text-decoration: underline;
        font-size: 18px;
        font-weight: 700;
        color: #000;
      }

      /* ── Info card ── */
      .info-card {
        background: #d9d9d9;
        border-radius: 20px;
        padding: 16px 20px 16px 12px;
        display: flex;
        flex-direction: column;
        gap: 0;
      }
      .info-row {
        display: flex;
        gap: 12px;
        align-items: flex-start;
        padding: 10px 0;
      }
      .info-icon {
        margin-top: 2px;
        flex-shrink: 0;
      }
      .info-divider {
        height: 1px;
        background: #e1e1e1;
        margin: 0 0 0 34px;
      }
      .info-label {
        font-size: 14px;
        font-weight: 600;
        color: #8e8e8e;
        margin: 0 0 3px;
      }
      .info-value {
        font-size: 14px;
        font-weight: 600;
        color: #000;
        margin: 0 0 2px;
      }
      .info-sub {
        font-size: 14px;
        font-weight: 500;
        color: #000;
        margin: 0;
      }

      /* ── Payment method cards ── */
      .method-list {
        display: flex;
        flex-direction: column;
        gap: 14px;
      }
      .method-card {
        display: flex;
        align-items: center;
        gap: 14px;
        border-radius: 20px;
        padding: 14px 18px;
        cursor: pointer;
      }
      .vietqr-card {
        background: #fbfbfb;
        border: 1px solid #000;
      }
      .vietqr-card.selected {
        border: 2px solid #000;
      }
      .paypal-card {
        background: #d9d9d9;
        border: 1px solid transparent;
      }
      .paypal-card.selected {
        border: 2px solid #000;
      }
      .method-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }
      .method-name {
        font-size: 14px;
        font-weight: 800;
        margin: 0 0 3px;
        color: #000;
      }
      .method-desc {
        font-size: 14px;
        font-weight: 300;
        color: #000;
        margin: 0;
      }

      /* ── QR area ── */
      .qr-area {
        display: flex;
        gap: 32px;
        align-items: flex-start;
      }
      .qr-loading {
        color: #777;
        font-size: 14px;
      }
      .qr-error {
        color: #c0392b;
        font-size: 14px;
      }
      .qr-card {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        min-width: 180px;
      }
      .vietqr-brand {
        font-size: 18px;
        font-weight: 800;
        letter-spacing: 1px;
      }
      .brand-v {
        color: #e31837;
      }
      .brand-rest {
        color: #003087;
      }
      .qr-img {
        width: 220px;
        height: 260px;
        object-fit: contain;
      }
      .qr-footer {
        display: flex;
        gap: 12px;
        font-size: 11px;
        color: #555;
      }
      .qr-bank {
        font-weight: 600;
      }

      /* ── Timer ── */
      .timer-block {
        display: flex;
        flex-direction: column;
        justify-content: center;
        padding-top: 32px;
      }
      .timer-label {
        font-size: 26px;
        font-weight: 800;
        color: #000;
        margin: 0 0 12px;
        line-height: 1.2;
      }
      .timer-value {
        font-size: 40px;
        font-weight: 800;
        color: #ff0000;
        margin: 0;
      }
      .timer-value.urgent {
        color: #c0392b;
      }

      /* ── Purchase button ── */
      .purchase-btn {
        display: block;
        width: 421px;
        padding: 18px;
        border-radius: 31px;
        background: #000;
        color: #fff;
        font-size: 20px;
        font-weight: 700;
        letter-spacing: 1px;
        border: none;
        cursor: pointer;
        transition: opacity 0.2s;
      }
      .purchase-btn:disabled {
        opacity: 0.45;
        cursor: not-allowed;
      }
      .purchase-btn:not(:disabled):hover {
        opacity: 0.82;
      }

      /* ── Order summary ── */
      .summary-card {
        background: #fff;
        border-radius: 35px;
        padding: 24px 28px;
        display: flex;
        flex-direction: column;
        gap: 10px;
      }
      .summary-title {
        font-size: 14px;
        font-weight: 700;
        letter-spacing: 1px;
        color: #6b6b6b;
        margin: 0;
      }
      .summary-divider {
        height: 1px;
        background: #8e8e8e;
        margin: 2px 0;
      }
      .summary-divider.light {
        background: #e1e1e1;
        opacity: 0.8;
      }

      .summary-items {
        display: flex;
        flex-direction: column;
        gap: 18px;
        max-height: 200px;
        overflow-y: auto;
      }
      .summary-item {
        display: flex;
        align-items: center;
        gap: 12px;
      }
      .item-thumb {
        width: 71px;
        height: 71px;
        object-fit: cover;
        border-radius: 6px;
        background: #eee;
        flex-shrink: 0;
      }
      .item-meta {
        flex: 1;
      }
      .item-name {
        font-size: 14px;
        font-weight: 700;
        margin: 0 0 4px;
        color: #000;
      }
      .item-qty {
        font-size: 12px;
        font-weight: 300;
        color: #000;
        margin: 0;
      }
      .item-price {
        font-size: 14px;
        font-weight: 700;
        white-space: nowrap;
        color: #000;
      }
      .summary-row {
        display: flex;
        justify-content: space-between;
        font-size: 15px;
        color: #000;
      }
      .total-row {
        font-size: 25px;
        font-weight: 700;
      }
      .toast {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 1100;
        padding: 12px 16px;
        border-radius: 8px;
        color: #fff;
        font-weight: 600;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
      }
      .toast.error {
        background: #c0392b;
      }
      .toast.success {
        background: #1e8449;
      }
    `,
  ],
})
export class PaymentComponent implements OnInit, OnDestroy {
  orderId: number = 0;
  selectedMethod: 'vietqr' | 'paypal' = 'vietqr';

  qrCode: any = null;
  isLoadingQR = false;
  qrError = '';
  isProcessing = false;

  timeLeft = 600; // 10 min in seconds
  private timerRef: ReturnType<typeof setInterval> | null = null;

  @ViewChild('qrCanvas') qrCanvas!: ElementRef<HTMLCanvasElement>;

  // Order data — received from delivery page via navigation state
  deliveryInfo: any = null;
  orderItems: any[] = [];
  subtotal = 0;
  vat = 0;
  shippingFee = 0;
  total = 0;
  totalWeight = 0;
  toastMessage = '';
  toastType: 'error' | 'success' = 'success';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private paymentService: PaymentService,
    private cartService: CartService,
    private checkoutSessionService: CheckoutSessionService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state ?? history.state;
    const paymentState = state?.['orderId']
      ? state
      : this.checkoutSessionService.getPendingPaymentState<PaymentNavigationState>();

    if (paymentState?.orderId) {
      this.restorePaymentState(paymentState);
      this.checkoutSessionService.setPendingOrderId(this.orderId);
    } else {
      this.route.params.subscribe((p) => {
        this.orderId = +p['orderId'];
      });
    }

    if (!this.orderId) {
      this.orderId = this.checkoutSessionService.getPendingOrderId();
    }

    const paypalToken = this.route.snapshot.queryParamMap.get('token');
    if (this.isPayPalCancelReturn(paypalToken)) {
      this.selectedMethod = 'paypal';
      this.isProcessing = false;
      this.clearPayPalReturnQuery();
      this.showToast('Thanh toán PayPal đã bị hủy. Bạn có thể tiếp tục với PayPal hoặc chọn VietQR.', 'error');
      return;
    }

    if (paypalToken && this.orderId) {
      this.selectedMethod = 'paypal';
      this.capturePayPalPayment(paypalToken);
      return;
    }

    this.loadVietQRCode();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  selectMethod(method: 'vietqr' | 'paypal'): void {
    this.selectedMethod = method;
    if (method === 'vietqr') {
      if (!this.qrCode) {
        this.loadVietQRCode();
      } else {
        setTimeout(() => this.renderQRCode(), 300);
      }
    }
  }

  loadVietQRCode(): void {
    if (!this.orderId) return;
    this.isLoadingQR = true;
    this.qrError = '';

    this.paymentService.generateVietQR(this.orderId).subscribe({
      next: (res) => {
        this.qrCode = res.result;
        this.isLoadingQR = false;
        this.timeLeft = 600;
        this.startTimer();
        setTimeout(() => {
          this.renderQRCode();
        }, 300);
      },
      error: (err) => {
        console.error('QR generation failed', err);
        this.qrError = 'Không thể tạo mã QR. Vui lòng thử lại.';
        this.isLoadingQR = false;
      },
    });
  }

  confirmVietQRPayment(): void {
    this.isProcessing = true;
    this.paymentService.simulateVietQRPayment(this.orderId).subscribe({
      next: (res) => {
        const order = res?.result?.order;
        const verdict = res?.result?.vietQrVerdict;
        if (order && order.orderStatus === 'PAYMENT_SUCCESS') {
          this.finalizeOrder();
        } else {
          this.isProcessing = false;
          const detail = verdict ? ` (${verdict})` : '';
          this.showToast(`Thanh toán chưa được xác nhận bởi VietQR.${detail}`, 'error');
        }
      },
      error: (err) => {
        this.isProcessing = false;
        console.error('VietQR simulation failed', err);
        this.showToast('Xác nhận thanh toán thử nghiệm thất bại. Vui lòng thử lại.', 'error');
      },
    });
  }

  private finalizeOrder(): void {
    this.paymentService.finalizePayment(this.orderId).subscribe({
      next: (res) => {
        this.isProcessing = false;
        this.stopTimer();
        this.cartService.clearCart();
        this.checkoutSessionService.clearPendingOrderId();
        this.checkoutSessionService.clearPendingPaymentState();
        this.checkoutSessionService.setCompletedOrder(res.result);
        this.router.navigate(['/order-success'], {
          state: { order: res.result },
        });
      },
      error: (err) => {
        this.isProcessing = false;
        console.error('Order finalization failed', err);
        this.showToast('Thanh toán đã được xác nhận nhưng hoàn tất đơn hàng thất bại. Vui lòng thử lại.', 'error');
      },
    });
  }

  redirectToPayPal(): void {
    this.isProcessing = true;
    this.checkoutSessionService.setPendingOrderId(this.orderId);
    this.checkoutSessionService.setPendingPaymentState(this.buildCurrentPaymentState());
    this.paymentService.createPayPalPayment(this.orderId).subscribe({
      next: (res) => {
        window.location.href = res.result?.url ?? res.result;
      },
      error: (err) => {
        this.isProcessing = false;
        console.error('PayPal init failed', err);
        this.showToast('Không thể khởi tạo thanh toán PayPal.', 'error');
      },
    });
  }

  private restorePaymentState(state: PaymentNavigationState): void {
    this.orderId = state.orderId;
    this.deliveryInfo = state.deliveryInfo;
    this.orderItems = state.orderItems ?? [];
    this.subtotal = state.subtotal ?? 0;
    this.vat = state.vat ?? 0;
    this.shippingFee = state.shippingFee ?? 0;
    this.total = state.total ?? 0;
    this.totalWeight = state.totalWeight ?? 0;
  }

  private buildCurrentPaymentState(): PaymentNavigationState {
    return {
      orderId: this.orderId,
      deliveryInfo: this.deliveryInfo,
      orderItems: this.orderItems,
      subtotal: this.subtotal,
      vat: this.vat,
      shippingFee: this.shippingFee,
      total: this.total,
      totalWeight: this.totalWeight,
    };
  }

  private capturePayPalPayment(token: string): void {
    this.isProcessing = true;
    this.paymentService.capturePayPalPayment(this.orderId, token).subscribe({
      next: () => {
        this.clearPayPalReturnQuery();
        this.finalizeOrder();
      },
      error: (err) => {
        this.isProcessing = false;
        console.error('PayPal capture failed', err);
        console.error('PayPal capture response body', err?.error);
        this.clearPayPalReturnQuery();
        const message = err?.error?.message ?? 'Thanh toán đã bị hủy hoặc thất bại.';
        this.showToast(message, 'error');
      },
    });
  }

  private isPayPalCancelReturn(token: string | null): boolean {
    const query = this.route.snapshot.queryParamMap;
    const status = query.get('paypalStatus')?.toLowerCase();
    const cancelled = query.get('paypalCancelled')?.toLowerCase();

    if (status === 'cancelled' || cancelled === 'true') {
      return true;
    }

    return Boolean(token) && !query.has('PayerID') && !query.has('payerId');
  }

  private clearPayPalReturnQuery(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {},
      replaceUrl: true,
    });
  }

  private renderQRCode(): void {
    if (!this.qrCode?.qrCode) return;
    const canvas = document.querySelector('canvas.qr-img') as HTMLCanvasElement;
    if (!canvas) {
      setTimeout(() => this.renderQRCode(), 200);
      return;
    }
    QRCode.toCanvas(canvas, this.qrCode.qrCode, { width: 220, margin: 1 });
  }

  private startTimer(): void {
    this.stopTimer();
    this.timerRef = setInterval(() => {
      if (this.timeLeft > 0) {
        this.timeLeft--;
        this.cdr.detectChanges();
      } else {
        this.stopTimer();
        this.qrCode = null;
        this.qrError = 'Mã QR đã hết hạn. Vui lòng tải lại trang để tạo mã mới.';
        this.cdr.detectChanges();
      }
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerRef !== null) {
      clearInterval(this.timerRef);
      this.timerRef = null;
    }
  }

  formatTime(s: number): string {
    const m = Math.floor(s / 60)
      .toString()
      .padStart(2, '0');
    const sec = (s % 60).toString().padStart(2, '0');
    return `${m}:${sec}`;
  }

  formatVND(amount: number): string {
    return amount.toLocaleString('vi-VN') + 'đ';
  }

  goBack(): void {
    this.router.navigate(['/delivery'], {
      state: { deliveryInfo: this.deliveryInfo },
    });
  }

  private showToast(message: string, type: 'error' | 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = '';
    }, 3500);
  }
}
