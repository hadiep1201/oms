import { Component, OnInit, computed, inject, signal, ViewChild, ElementRef, effect } from '@angular/core';
import JsBarcode from 'jsbarcode';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Product } from '../../schemas/order.schema';
import { ProductMetadataUtil } from '../../utils/product-metadata.util';


@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css'],})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private location = inject(Location);
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);

  @ViewChild('barcodeSvg') barcodeSvg!: ElementRef;

  constructor() {
    effect(() => {
      const p = this.product();
      if (p && p.barcode) {
        setTimeout(() => {
          if (this.barcodeSvg && this.barcodeSvg.nativeElement) {
            JsBarcode(this.barcodeSvg.nativeElement, p.barcode, {
              format: 'CODE128',
              lineColor: '#000',
              width: 2,
              height: 40,
              displayValue: true
            });
          }
        }, 100);
      }
    });
  }

  canUseStorefrontCart = computed(() => {
    this.authService.session();
    return this.authService.canUseStorefrontCart();
  });

  loading = signal<boolean>(true);
  errorMessage = signal<string>('');
  product = signal<any | null>(null);
  quantity = signal<number>(1);
  toastMessage = signal<string>('');
  fallbackImage = 'placeholder-product.svg';

  dynamicTechnicalDetails() {
    return ProductMetadataUtil.getDynamicTechnicalDetails(this.product());
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const idStr = params.get('id');
      if (idStr) {
        const id = Number(idStr);
        this.fetchProductDetail(id);
      } else {
        this.errorMessage.set('Invalid Product ID');
        this.loading.set(false);
      }
    });
  }

  fetchProductDetail(id: number) {
    this.loading.set(true);
    this.errorMessage.set('');
    this.productService.getProductDetail(id).subscribe({
      next: (res) => {
        if (res && res.result) {
          this.product.set(res.result);
        } else {
          this.errorMessage.set('Product not found');
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading product details:', err);
        this.errorMessage.set('Could not load product details.');
        this.loading.set(false);
      }
    });
  }

  isOutOfStock(): boolean {
    return ProductMetadataUtil.isOutOfStock(this.product());
  }

  increaseQty() {
    const max = this.product()?.stockQuantity || 1;
    if (this.quantity() < max) {
      this.quantity.update(q => q + 1);
    }
  }

  decreaseQty() {
    if (this.quantity() > 1) {
      this.quantity.update(q => q - 1);
    }
  }

  addToCart() {
    const p = this.product();
    if (!p) return;

    this.cartService.addDetailToCart(p, this.quantity()).then((result) => {
      if (result.ok) {
        this.showToast(`Added ${this.quantity()} x "${p.title}" to cart!`);
        return;
      }
      this.showToast(result.message ?? 'Cannot add this item to cart.');
    });
  }

  goBack() {
    this.location.back();
  }

  useFallbackImage(event: Event): void {
    const image = event.target as HTMLImageElement;
    if (!image.src.endsWith(this.fallbackImage)) {
      image.src = this.fallbackImage;
    }
  }



  private showToast(msg: string) {
    this.toastMessage.set(msg);
    setTimeout(() => {
      this.toastMessage.set('');
    }, 3000);
  }
}
