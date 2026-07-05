import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { AddProductModalComponent } from '../../components/add-product-modal/add-product-modal.component';
import { DeleteProductsSummary, ManagerProduct } from '../../schemas/product.schema';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-manager-product-list',
  standalone: true,
  imports: [CurrencyPipe, NzIconModule, AddProductModalComponent],
  templateUrl: './manager-product-list.component.html',
  styleUrls: ['./manager-product-list.component.css'],
})
/**
 * Coupling:
 * - Data coupling with FE ProductService (loads product list, triggers deleteProducts toward ProductCommandController)
 * - Data coupling with AddProductModalComponent (opens create/edit modal)
 * Cohesion: Functional cohesion
 * Reason: UI page dedicated to manager product list, selection, delete confirmation, and opening CUD modals.
 *
 * SOLID Review:
 * - SRP: No clear violation. Page handles manager product list display, selection, delete flow, and modal orchestration.
 * - OCP: No clear violation. New list actions can be added as separate handlers without changing core table logic.
 * - LSP/ISP: not applicable.
 * - DIP risk: depends directly on concrete ProductService (loads list via GET /api/products/manager) rather than
 *   narrow ProductListDataSource / ProductCommandApi interfaces.
 *   Impact: harder to mock or swap data sources in unit tests.
 * Improvement direction: inject ProductListDataSource and ProductCommandApi abstractions.
 */
export class ManagerProductListComponent implements OnInit {
  private productService = inject(ProductService);

  products = signal<ManagerProduct[]>([]);
  selectedIds = signal<number[]>([]);
  showAddModal = signal(false);
  showDeleteConfirm = signal(false);
  showDeleteSummary = signal(false);
  deleteError = signal('');
  deleteSummary = signal<DeleteProductsSummary | null>(null);
  editingProductId = signal<number | null>(null);
  loading = signal(false);
  loadError = signal('');

  hasSelection = computed(() => this.selectedIds().length > 0);

  selectableProducts = computed(() => this.products().filter((p) => !this.isDeactivated(p)));

  isAllSelected = computed(() => {
    const selectable = this.selectableProducts();
    return (
      selectable.length > 0 && selectable.every((p) => this.selectedIds().includes(p.id))
    );
  });

  isIndeterminate = computed(() => {
    const count = this.selectedIds().length;
    const total = this.selectableProducts().length;
    return count > 0 && count < total;
  });

  ngOnInit(): void {
    this.loadProducts();
  }

  isSelected(id: number): boolean {
    return this.selectedIds().includes(id);
  }

  isDeactivated(product: ManagerProduct): boolean {
    const status = product.status?.toUpperCase();
    return status === 'DELETED' || status === 'DEACTIVATED';
  }

  onSelectChange(id: number, event: Event): void {
    const product = this.products().find((p) => p.id === id);
    if (product && this.isDeactivated(product)) {
      return;
    }

    const checked = (event.target as HTMLInputElement).checked;
    this.selectedIds.update((ids) => {
      const next = new Set(ids);
      if (checked) {
        next.add(id);
      } else {
        next.delete(id);
      }
      return [...next];
    });
  }

  onSelectAllChange(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedIds.set(this.selectableProducts().map((p) => p.id));
    } else {
      this.selectedIds.set([]);
    }
  }

  openDeleteConfirm(): void {
    this.deleteError.set('');
    if (!this.selectedIds().length) {
      return;
    }
    if (this.selectedIds().length > 10) {
      this.deleteError.set('Không thể chọn nhiều hơn 10 sản phẩm. Vui lòng bỏ chọn bớt.');
    }
    this.showDeleteConfirm.set(true);
  }

  closeDeleteConfirm(): void {
    this.showDeleteConfirm.set(false);
    this.deleteError.set('');
  }

  confirmDelete(): void {
    const ids = this.selectedIds();
    if (!ids.length) {
      this.closeDeleteConfirm();
      return;
    }

    this.deleteError.set('');

    this.productService.deleteProducts(ids).subscribe({
      next: (summary) => {
        this.closeDeleteConfirm();
        this.selectedIds.set([]);
        this.deleteSummary.set(summary);
        this.showDeleteSummary.set(true);
        this.loadProducts();
      },
      error: (err: Error) => {
        this.deleteError.set(err.message ?? 'Xóa sản phẩm thất bại. Vui lòng thử lại.');
      },
    });
  }

  deleteSummaryMessage(): string {
    const summary = this.deleteSummary();
    if (!summary) {
      return '';
    }

    let message = `Đã xóa ${summary.deletedCount} sản phẩm, đã vô hiệu hóa ${summary.deactivatedCount} sản phẩm.`;
    if (summary.failed.length) {
      const errors = summary.failed
        .map((f) => f.errorMessage)
        .filter(Boolean)
        .join('; ');
      if (errors) {
        message += ` Lỗi: ${errors}`;
      }
    }
    return message;
  }

  closeDeleteSummary(): void {
    this.showDeleteSummary.set(false);
    this.deleteSummary.set(null);
  }

  openAddModal(): void {
    this.showAddModal.set(true);
  }

  closeAddModal(): void {
    this.showAddModal.set(false);
  }

  openEditModal(productId: number): void {
    this.editingProductId.set(productId);
  }

  closeEditModal(): void {
    this.editingProductId.set(null);
  }

  onProductSaved(): void {
    this.loadProducts();
  }

  formatStatus(status: string): string {
    if (!status) return '';
    const upper = status.toUpperCase();
    if (upper === 'DEACTIVATED' || upper === 'DELETED') {
      return 'Đã vô hiệu hóa';
    }
    if (upper === 'ACTIVE') {
      return 'Đang hoạt động';
    }
    return status;
  }

  private loadProducts(): void {
    this.loading.set(true);
    this.loadError.set('');
    this.productService.getProducts().subscribe({
      next: (list) => {
        this.products.set(list);
        const validIds = new Set(list.map((p) => p.id));
        this.selectedIds.update((ids) => ids.filter((id) => validIds.has(id)));
        this.loading.set(false);
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.loading.set(false);
        this.products.set([]);
        this.loadError.set(
          err?.error?.message ??
            err?.message ??
            'Không thể tải danh sách sản phẩm. Backend đã khởi chạy ở cổng 8080 chưa?',
        );
      },
    });
  }
}
