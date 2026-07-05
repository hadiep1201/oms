import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  ApiResponse,
  CreateProductRequest,
  DeleteProductRequest,
  ProductResponse,
  UpdateProductRequest,
} from '../schemas/product.schema';

/**
 * SRP: write-only product API calls and image upload.
 * Coupling: data coupling with ProductCommandController via HttpClient.
 * Cohesion: functional cohesion.
 */
@Injectable({ providedIn: 'root' })
export class ProductCommandApiService {
  private http = inject(HttpClient);
  private readonly backendOrigin = 'http://localhost:8080';
  private readonly apiUrl = `${this.backendOrigin}/oms/api/products`;
  private readonly filesApiUrl = `${this.backendOrigin}/oms/api/files`;

  createProduct(request: CreateProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.http.post<ApiResponse<ProductResponse>>(this.apiUrl, request);
  }

  updateProduct(id: number, request: UpdateProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.http.put<ApiResponse<ProductResponse>>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(
    id: number,
    request: DeleteProductRequest,
  ): Observable<ApiResponse<{ id: number; status: string; message?: string }>> {
    return this.http.delete<ApiResponse<{ id: number; status: string; message?: string }>>(
      `${this.apiUrl}/${id}`,
      { body: request },
    );
  }

  uploadProductImage(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<{ url: string }>>(`${this.filesApiUrl}/upload/image`, formData)
      .pipe(
        map((res) => {
          const path = res.result?.url ?? '';
          if (!path) {
            throw new Error('Upload failed: empty URL');
          }
          return path.startsWith('http') ? path : `${this.backendOrigin}${path}`;
        }),
      );
  }
}
