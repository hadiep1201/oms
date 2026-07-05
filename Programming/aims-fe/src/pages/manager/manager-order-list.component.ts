import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { ManagerOrderResponse } from '../../schemas/order.schema';
import { ManagerOrderService } from '../../services/manager-order.service';

@Component({
  selector: 'app-manager-order-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, NzIconModule],
  templateUrl: './manager-order-list.component.html',
  styleUrls: ['./manager-order-list.component.css'],
})
export class ManagerOrderListComponent implements OnInit {
  private managerOrderService = inject(ManagerOrderService);
  private readonly pageSize = 30;

  orders = signal<ManagerOrderResponse[]>([]);
  selectedOrder = signal<ManagerOrderResponse | null>(null);
  approveTarget = signal<ManagerOrderResponse | null>(null);
  rejectTarget = signal<ManagerOrderResponse | null>(null);
  actionOrderId = signal<number | null>(null);
  page = signal(0);
  totalPages = signal(0);
  loading = signal(false);
  loadError = signal<string | null>(null);
  actionError = signal<string | null>(null);
  rejectReason = '';

  canGoPrevious = computed(() => this.page() > 0);
  canGoNext = computed(() => this.page() + 1 < this.totalPages());

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.managerOrderService.getPendingOrders(this.page(), this.pageSize).subscribe({
      next: (response) => {
        const result = response.result;
        this.orders.set(result.orders ?? []);
        this.page.set(result.page ?? 0);
        this.totalPages.set(result.totalPages ?? 0);
        this.loading.set(false);
      },
      error: (error) => {
        this.loadError.set(this.extractError(error));
        this.loading.set(false);
      },
    });
  }

  openDetails(orderId: number): void {
    this.actionError.set(null);
    this.managerOrderService.getOrder(orderId).subscribe({
      next: (response) => this.selectedOrder.set(response.result),
      error: (error) => this.actionError.set(this.extractError(error)),
    });
  }

  closeDetails(): void {
    this.selectedOrder.set(null);
  }

  openApproveConfirm(order: ManagerOrderResponse): void {
    this.closeDetails();
    this.actionError.set(null);
    this.approveTarget.set(order);
  }

  closeApproveConfirm(): void {
    this.approveTarget.set(null);
  }

  confirmApprove(): void {
    const order = this.approveTarget();
    if (!order) {
      return;
    }

    this.actionOrderId.set(order.orderId);
    this.managerOrderService.approveOrder(order.orderId).subscribe({
      next: () => {
        this.actionOrderId.set(null);
        this.closeApproveConfirm();
        this.loadOrders();
      },
      error: (error) => {
        this.actionError.set(this.extractError(error));
        this.actionOrderId.set(null);
      },
    });
  }

  openRejectConfirm(order: ManagerOrderResponse): void {
    this.closeDetails();
    this.actionError.set(null);
    this.rejectReason = '';
    this.rejectTarget.set(order);
  }

  closeRejectConfirm(): void {
    this.rejectTarget.set(null);
    this.rejectReason = '';
  }

  confirmReject(): void {
    const order = this.rejectTarget();
    if (!order) {
      return;
    }

    this.actionOrderId.set(order.orderId);
    this.managerOrderService.rejectOrder(order.orderId, this.rejectReason).subscribe({
      next: () => {
        this.actionOrderId.set(null);
        this.closeRejectConfirm();
        this.loadOrders();
      },
      error: (error) => {
        this.actionError.set(this.extractError(error));
        this.actionOrderId.set(null);
      },
    });
  }

  goPrevious(): void {
    if (!this.canGoPrevious()) {
      return;
    }
    this.page.set(this.page() - 1);
    this.loadOrders();
  }

  goNext(): void {
    if (!this.canGoNext()) {
      return;
    }
    this.page.set(this.page() + 1);
    this.loadOrders();
  }

  isActionRunning(orderId: number): boolean {
    return this.actionOrderId() === orderId;
  }

  formatPayment(paymentMethod?: string): string {
    return paymentMethod ? paymentMethod.toUpperCase() : 'N/A';
  }

  formatStatus(status?: string): string {
    return status ? status.replaceAll('_', ' ') : 'N/A';
  }

  private extractError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message || error.error?.error || error.message || 'Request failed.';
    }
    if (error instanceof Error) {
      return error.message;
    }
    return 'Request failed.';
  }
}
