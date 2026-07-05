import { Component, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzIconModule } from 'ng-zorro-antd/icon';
import {
  CreateProductRequest,
  ProductFormValue,
  ProductType,
  UpdateProductRequest,
} from '../../schemas/product.schema';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';

const PRODUCT_TYPES: { value: ProductType; label: string }[] = [
  { value: 'BOOK', label: 'Book' },
  { value: 'DVD', label: 'DVD' },
  { value: 'CD', label: 'CD' },
  { value: 'NEWSPAPER', label: 'Newspaper' },
];

function productTypeToCategory(productType: ProductType): string {
  return PRODUCT_TYPES.find((t) => t.value === productType)?.label ?? productType;
}

function categoryToProductType(category: string): ProductType | '' {
  const normalized = category.trim().toLowerCase();
  const match = PRODUCT_TYPES.find(
    (t) => t.label.toLowerCase() === normalized || t.value.toLowerCase() === normalized,
  );
  return match?.value ?? '';
}

@Component({
  selector: 'app-add-product-modal',
  standalone: true,
  imports: [ReactiveFormsModule, NzIconModule],
  templateUrl: './add-product-modal.component.html',
  styleUrls: ['./add-product-modal.component.css'],
})
/**
 * Coupling:
 * - Data coupling with FE ProductService, which calls ProductCommandController (CreateProductRequest / UpdateProductRequest)
 * - Control coupling via mode input ('create' | 'edit') to choose create vs update API operation
 * Cohesion: Functional cohesion
 * Reason: Component exists solely to collect product form data and submit Create or Update Product.
 *
 * SOLID Review:
 * - SRP: No clear violation. UI component for create/edit product form submission only.
 * - OCP risk: form template and buildFormValue() handle all four product types inline; adding a new
 *   product type requires editing this component (fields, validators, category/productType mapping).
 *   Impact: extension of product catalog couples to UI changes in one large component.
 * - ISP risk: category is derived from productType on submit (single dropdown); API still requires both fields.
 * - LSP/ISP/DIP: DIP not applicable at component level (uses concrete ProductService).
 * Improvement direction: extract per-type form sections or a ProductFormStrategy registry so new types
 *   can be added without modifying the core modal shell.
 */
export class AddProductModalComponent {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private authService = inject(AuthService);

  mode = input<'create' | 'edit'>('create');
  productId = input<number | null>(null);

  closed = output<void>();
  saved = output<void>();

  productTypes = PRODUCT_TYPES;
  submitting = signal(false);
  loading = signal(false);
  uploadingImage = signal(false);
  errorMessage = signal('');

  form = this.fb.nonNullable.group({
    productType: ['' as ProductType | '', Validators.required],
    title: ['', Validators.required],
    generalDescription: [''],
    barcode: [''],
    imageUrl: [''],
    originalValue: [0, [Validators.required, Validators.min(0.01)]],
    currentPrice: [0, [Validators.required, Validators.min(0.01)]],
    weight: [0, [Validators.required, Validators.min(0.01)]],
    length: [0, [Validators.required, Validators.min(0.01)]],
    height: [0, [Validators.required, Validators.min(0.01)]],
    width: [0, [Validators.required, Validators.min(0.01)]],
    stockQuantity: [0, [Validators.required, Validators.min(0)]],
    status: ['ACTIVE'],
    publicationDate: [''],
    language: [''],
    publisher: [''],
    coverType: [''],
    nbPages: [null as number | null],
    genre: [''],
    editorInChief: [''],
    issueNumber: [''],
    publicationFrequency: [''],
    issn: [''],
    releaseDate: [''],
    artists: [''],
    recordLabel: [''],
    discType: [''],
    director: [''],
    runtime: [null as number | null],
    studio: [''],
    subtitles: [''],
  });

  selectedType = signal<ProductType | null>(null);

  constructor() {
    this.form.controls.productType.valueChanges.subscribe((v) => {
      this.selectedType.set(v ? (v as ProductType) : null);
    });

    effect(() => {
      const isEdit = this.mode() === 'edit';
      const id = this.productId();

      if (isEdit && id) {
        this.loading.set(true);
        this.errorMessage.set('');
        this.productService.getProductDetail(id).subscribe({
          next: (res) => {
            this.patchForm(res.result);
            this.form.controls.productType.disable();
            this.loading.set(false);
          },
          error: () => {
            this.errorMessage.set('Không thể tải chi tiết sản phẩm.');
            this.loading.set(false);
          },
        });
      } else {
        this.form.controls.productType.enable();
      }
    });
  }

  detailLabel(): string {
    const t = this.selectedType();
    return t ? this.productTypes.find((p) => p.value === t)?.label.toUpperCase() ?? t : '';
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close();
    }
  }

  close(): void {
    this.closed.emit();
  }

  onImageFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.uploadingImage.set(true);
    this.errorMessage.set('');

    this.productService.uploadProductImage(file).subscribe({
      next: (url) => {
        this.form.patchValue({ imageUrl: url });
        this.uploadingImage.set(false);
        input.value = '';
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.uploadingImage.set(false);
        input.value = '';
        this.errorMessage.set(
          err?.error?.message ?? err?.message ?? 'Tải ảnh lên thất bại. Backend đang chạy ở cổng 8080 chứ?',
        );
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.buildFormValue();
    this.submitting.set(true);
    this.errorMessage.set('');

    if (this.mode() === 'edit') {
      const id = this.productId();
      if (!id) {
        this.submitting.set(false);
        return;
      }

      const userId = this.authService.getUserId();
      if (!userId) {
        this.submitting.set(false);
        this.errorMessage.set('Bạn phải đăng nhập để cập nhật sản phẩm.');
        return;
      }

      const payload: UpdateProductRequest = {
        ...formValue,
        updatedByUserId: userId,
      };

      this.productService.updateProduct(id, payload).subscribe({
        next: () => this.onSaveSuccess(),
        error: (err) => this.onSaveError(err, 'update'),
      });
      return;
    }

    const userId = this.authService.getUserId();
    if (!userId) {
      this.submitting.set(false);
      this.errorMessage.set('Bạn phải đăng nhập để tạo sản phẩm.');
      return;
    }

    const payload: CreateProductRequest = {
      ...formValue,
      createdByUserId: userId,
    };

    this.productService.createProduct(payload).subscribe({
      next: () => this.onSaveSuccess(),
      error: (err) => this.onSaveError(err, 'create'),
    });
  }

  private patchForm(detail: ProductFormValue & { pages?: number }): void {
    const productType =
      detail.productType || categoryToProductType(detail.category ?? '');

    this.form.patchValue({
      productType,
      title: detail.title,
      generalDescription: detail.generalDescription ?? '',
      barcode: detail.barcode ?? '',
      imageUrl: detail.imageUrl ?? '',
      originalValue: detail.originalValue,
      currentPrice: detail.currentPrice,
      weight: detail.weight,
      length: detail.length,
      height: detail.height,
      width: detail.width,
      stockQuantity: detail.stockQuantity,
      status: detail.status ?? 'ACTIVE',
      publicationDate: detail.publicationDate ?? '',
      language: detail.language ?? '',
      publisher: detail.publisher ?? '',
      coverType: detail.coverType ?? '',
      nbPages: detail.nbPages ?? detail.pages ?? null,
      genre: detail.genre ?? '',
      editorInChief: detail.editorInChief ?? '',
      issueNumber: detail.issueNumber ?? '',
      publicationFrequency: detail.publicationFrequency ?? '',
      issn: detail.issn ?? '',
      releaseDate: detail.releaseDate ?? '',
      artists: detail.artists ?? '',
      recordLabel: detail.recordLabel ?? '',
      discType: detail.discType ?? '',
      director: detail.director ?? '',
      runtime: detail.runtime ?? null,
      studio: detail.studio ?? '',
      subtitles: detail.subtitles ?? '',
    });
    this.selectedType.set(productType || null);
  }

  private buildFormValue(): ProductFormValue {
    const v = this.form.getRawValue();
    const productType = (v.productType || this.form.controls.productType.value) as ProductType;
    return {
      productType,
      title: v.title,
      category: productTypeToCategory(productType),
      generalDescription: v.generalDescription || undefined,
      barcode: v.barcode || undefined,
      imageUrl: v.imageUrl || undefined,
      originalValue: v.originalValue,
      currentPrice: v.currentPrice,
      weight: v.weight,
      length: v.length,
      height: v.height,
      width: v.width,
      stockQuantity: v.stockQuantity,
      status: v.status || 'ACTIVE',
      publicationDate: v.publicationDate || undefined,
      language: v.language || undefined,
      publisher: v.publisher || undefined,
      coverType: v.coverType || undefined,
      nbPages: v.nbPages ?? undefined,
      genre: v.genre || undefined,
      editorInChief: v.editorInChief || undefined,
      issueNumber: v.issueNumber || undefined,
      publicationFrequency: v.publicationFrequency || undefined,
      issn: v.issn || undefined,
      releaseDate: v.releaseDate || undefined,
      artists: v.artists || undefined,
      recordLabel: v.recordLabel || undefined,
      discType: v.discType || undefined,
      director: v.director || undefined,
      runtime: v.runtime ?? undefined,
      studio: v.studio || undefined,
      subtitles: v.subtitles || undefined,
    };
  }

  private onSaveSuccess(): void {
    this.submitting.set(false);
    this.saved.emit();
    this.close();
  }

  private onSaveError(err: { error?: { message?: string } }, action: 'create' | 'update'): void {
    this.submitting.set(false);
    this.errorMessage.set(
      err?.error?.message ??
        'Lưu sản phẩm thất bại. Vui lòng kiểm tra lại thông tin.',
    );
  }
}
