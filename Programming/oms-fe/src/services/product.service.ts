import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, forkJoin, map, Observable, of, switchMap, throwError } from 'rxjs';
import {
  ApiResponse,
  CreateProductRequest,
  DeleteProductOutcome,
  DeleteProductsSummary,
  ManagerProduct,
  ProductDetail,
  ProductHomepage,
  ProductResponse,
  UpdateProductRequest,
} from '../schemas/product.schema';
import { ProductSearchResponse } from '../schemas/order.schema';
import { AuthService } from './auth.service';
import { DeleteQuotaService } from './delete-quota.service';
import { ProductCommandApiService } from './product-command-api.service';

const MAX_PRODUCTS_PER_DELETE = 10;
const MAX_DAILY_DELETES = 20;

/**
 * Storefront/manager read operations stay here (out of CUD scope).
 * CUD write operations delegate to ProductCommandApiService and DeleteQuotaService.
 */
@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private commandApi = inject(ProductCommandApiService);
  private deleteQuotaService = inject(DeleteQuotaService);
  private authService = inject(AuthService);
  private readonly apiUrl = 'http://localhost:8080/oms/api/products';

  getProducts(): Observable<ManagerProduct[]> {
    return this.http
      .get<ApiResponse<ManagerProduct[]>>(`${this.apiUrl}/manager`)
      .pipe(
        map((res) =>
          (res.result ?? []).map((p) => ({
            ...p,
            currentPrice: Number(p.currentPrice),
            stockQuantity: Number(p.stockQuantity),
          })),
        ),
      );
  }

  getFeaturedProducts(): Observable<ApiResponse<ProductHomepage[]>> {
    return this.http.get<ApiResponse<ProductHomepage[]>>(`${this.apiUrl}/featured`);
  }

  searchProducts(keyword: string, minPrice?: number, maxPrice?: number): Observable<ApiResponse<ProductSearchResponse[]>> {
    let params = new HttpParams().set('keyword', keyword);
    if (minPrice != null) {
      params = params.set('minPrice', minPrice);
    }
    if (maxPrice != null) {
      params = params.set('maxPrice', maxPrice);
    }
    return this.http.get<ApiResponse<ProductSearchResponse[]>>(`${this.apiUrl}/search`, { params });
  }

  getProductDetail(id: number): Observable<ApiResponse<ProductDetail>> {
    return this.http.get<ApiResponse<ProductDetail>>(`${this.apiUrl}/${id}`);
  }

  createProduct(request: CreateProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.commandApi.createProduct(request);
  }

  updateProduct(id: number, request: UpdateProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.commandApi.updateProduct(id, request);
  }

  uploadProductImage(file: File): Observable<string> {
    return this.commandApi.uploadProductImage(file);
  }

  deleteProducts(ids: number[]): Observable<DeleteProductsSummary> {
    if (ids.length > MAX_PRODUCTS_PER_DELETE) {
      return throwError(
        () => new Error('Cannot select more than 10 products. Please reduce your selection.'),
      );
    }

    const dailyCount = this.deleteQuotaService.getDailyDeleteCount();
    if (dailyCount + ids.length > MAX_DAILY_DELETES) {
      return throwError(() => new Error('Cannot delete more than 20 products per day.'));
    }

    return this.getProducts().pipe(
      switchMap((products) => {
        const byId = new Map(products.map((p) => [p.id, p]));
        return forkJoin(ids.map((id) => this.deleteOneProduct(byId.get(id), id))).pipe(
          map((outcomes) => {
            const summary = this.summarizeOutcomes(outcomes);
            this.deleteQuotaService.incrementDailyDeleteCount(
              summary.deletedCount + summary.deactivatedCount,
            );
            return summary;
          }),
        );
      }),
    );
  }

  private deleteOneProduct(
    product: ManagerProduct | undefined,
    id: number,
  ): Observable<DeleteProductOutcome> {
    if (!product) {
      return of({ productId: id, kind: 'failed', errorMessage: 'Product not found' });
    }

    if (this.isDeactivatedStatus(product.status)) {
      return of({
        productId: id,
        kind: 'failed',
        errorMessage: 'Product is already deactivated',
      });
    }

    const userId = this.authService.getUserId();
    if (!userId) {
      return throwError(() => new Error('You must be logged in to delete products.'));
    }

    return this.commandApi.deleteProduct(id, { deletedByUserId: userId }).pipe(
      map((res) => {
        const status = res.result?.status?.toUpperCase() ?? '';
        const kind = status === 'DELETED' || product.stockQuantity === 0 ? 'deleted' : 'deactivated';
        return { productId: id, kind: kind as 'deleted' | 'deactivated' };
      }),
      catchError((err) => {
        const message = err?.error?.message ?? err?.message ?? 'Failed to delete product';
        return of({
          productId: id,
          kind: 'failed' as const,
          errorMessage: message,
        });
      }),
    );
  }

  private summarizeOutcomes(outcomes: DeleteProductOutcome[]): DeleteProductsSummary {
    let deletedCount = 0;
    let deactivatedCount = 0;
    const failed: DeleteProductOutcome[] = [];

    for (const o of outcomes) {
      if (o.kind === 'deleted') {
        deletedCount++;
      } else if (o.kind === 'deactivated') {
        deactivatedCount++;
      } else {
        failed.push(o);
      }
    }

    return { deletedCount, deactivatedCount, failed };
  }

  private isDeactivatedStatus(status: string): boolean {
    const s = status?.toUpperCase();
    return s === 'DEACTIVATED' || s === 'DELETED';
  }
}
