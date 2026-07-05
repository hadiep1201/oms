import { Component, OnInit, computed, inject, signal, effect } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { ProductSearchResponse, Product } from '../../schemas/order.schema';
import { ProductMetadataUtil } from '../../utils/product-metadata.util';


@Component({
  selector: 'app-search-product',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule, RouterLink],
  templateUrl: './search-product.component.html',
  styleUrls: ['./search-product.component.css'],})
export class SearchProductComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private authService = inject(AuthService);

  canUseStorefrontCart = computed(() => {
    this.authService.session();
    return this.authService.canUseStorefrontCart();
  });

  // States
  keyword = signal<string>('');
  products = signal<ProductSearchResponse[]>([]);
  loading = signal<boolean>(false);
  toastMessage = signal<string>('');

  // Filter bindings
  priceFrom: number | null = null;
  priceTo: number | null = null;

  ngOnInit() {
    // Listen to query parameters changes
    this.route.queryParams.subscribe(params => {
      const kw = params['keyword'] || '';
      this.keyword.set(kw);
      
      // Extract minPrice and maxPrice from query parameters
      this.priceFrom = params['minPrice'] != null ? Number(params['minPrice']) : null;
      this.priceTo = params['maxPrice'] != null ? Number(params['maxPrice']) : null;

      this.fetchProducts();
    });
  }

  fetchProducts() {
    const kw = this.keyword();
    if (!kw.trim()) {
      this.products.set([]);
      return;
    }

    this.loading.set(true);

    let minPrice: number | undefined = undefined;
    let maxPrice: number | undefined = undefined;

    if (this.priceFrom !== null || this.priceTo !== null) {
      minPrice = this.priceFrom !== null ? this.priceFrom : 0;
      maxPrice = this.priceTo !== null ? this.priceTo : 999999999;
    }

    this.productService.searchProducts(kw, minPrice, maxPrice).subscribe({
      next: (res: any) => {
        this.products.set(res.result || []);
        this.loading.set(false);
      },
      error: (err: any) => {
        console.error('Error searching products:', err);
        this.products.set([]);
        this.loading.set(false);
      }
    });
  }

  applyFilter() {
    // Navigate to same route but with updated query params to trigger subscription
    const queryParams: any = {
      keyword: this.keyword()
    };

    if (this.priceFrom !== null || this.priceTo !== null) {
      queryParams.minPrice = this.priceFrom !== null ? this.priceFrom : 0;
      queryParams.maxPrice = this.priceTo !== null ? this.priceTo : 999999999;
    }

    this.router.navigate(['/search'], { queryParams });
  }

  clearFilter() {
    this.priceFrom = null;
    this.priceTo = null;
    this.router.navigate(['/search'], { queryParams: { keyword: this.keyword() } });
  }

  isOutOfStock(item: ProductSearchResponse): boolean {
    return ProductMetadataUtil.isOutOfStock(item);
  }

  addToCart(item: ProductSearchResponse) {
    this.loading.set(true);
    // Fetch full product details from backend to ensure weight/dimensions are accurate for shipping calculations
    this.productService.getProductDetail(item.id).subscribe({
      next: (res: any) => {
        this.cartService.addDetailToCart(res.result).then((result) => {
          this.loading.set(false);
          if (result.ok) {
            this.showToast(`Added "${item.title}" to cart!`);
            return;
          }
          this.showToast(result.message ?? 'Cannot add this item to cart.');
        });
      },
      error: (err: any) => {
        console.error('Error fetching product detail for cart:', err);
        // Fallback: Map item to Product with defaults
        this.cartService.addDetailToCart(item).then((result) => {
          this.loading.set(false);
          if (result.ok) {
            this.showToast(`Added "${item.title}" to cart!`);
            return;
          }
          this.showToast(result.message ?? 'Cannot add this item to cart.');
        });
      }
    });
  }

  private showToast(msg: string) {
    this.toastMessage.set(msg);
    setTimeout(() => {
      this.toastMessage.set('');
    }, 3000);
  }
}
