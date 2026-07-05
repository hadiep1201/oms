import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Product } from '../../schemas/order.schema';
import { ProductHomepage } from '../../schemas/product.schema';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);

  canUseStorefrontCart = computed(() => {
    this.authService.session();
    return this.authService.canUseStorefrontCart();
  });

  products = signal<ProductHomepage[]>([]);
  isLoading = signal(true);
  errorMessage = signal('');
  toastMessage = signal('');
  fallbackImage = 'placeholder-product.svg';

  ngOnInit(): void {
    this.productService.getFeaturedProducts().subscribe({
      next: (res) => {
        this.products.set(res.result ?? []);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load featured products', err);
        this.errorMessage.set('Không thể tải danh sách sản phẩm nổi bật.');
        this.isLoading.set(false);
      },
    });
  }

  formatPrice(value: number | null): string {
    if (value === null || value === undefined) {
      return '0\u0111';
    }

    return value.toLocaleString('vi-VN') + '\u0111';
  }

  useFallbackImage(event: Event): void {
    const image = event.target as HTMLImageElement;
    if (!image.src.endsWith(this.fallbackImage)) {
      image.src = this.fallbackImage;
    }
  }

  addToCart(product: ProductHomepage): void {
    this.productService.getProductDetail(product.id).subscribe({
      next: (res) => {
        this.cartService.addDetailToCart(res.result).then((result) => {
          this.showToast(result.ok ? `Đã thêm "${product.title}" vào giỏ hàng!` : (result.message ?? 'Không thể thêm sản phẩm này vào giỏ hàng.'));
        });
      },
      error: (err) => {
        console.error('Failed to fetch product detail for cart', err);
        this.showToast('Không thể thêm sản phẩm này vào giỏ hàng.');
      }
    });
  }

  private showToast(message: string): void {
    this.toastMessage.set(message);
    setTimeout(() => this.toastMessage.set(''), 3000);
  }
}
