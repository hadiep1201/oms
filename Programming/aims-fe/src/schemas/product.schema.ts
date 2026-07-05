export type ProductStatus = 'ACTIVE' | 'DELETED' | string;

export type ProductType = 'BOOK' | 'NEWSPAPER' | 'CD' | 'DVD';

export interface ManagerProduct {
  id: number;
  title: string;
  category: string;
  currentPrice: number;
  stockQuantity: number;
  status: ProductStatus;
}

export interface ProductHomepage {
  id: number;
  title: string;
  category: string;
  imageUrl: string | null;
  originalValue: number | null;
  currentPrice: number | null;
}

export interface CreateProductRequest {
  productType: ProductType;
  title: string;
  category: string;
  generalDescription?: string;
  barcode?: string;
  imageUrl?: string;
  originalValue: number;
  currentPrice: number;
  weight: number;
  length: number;
  height: number;
  width: number;
  stockQuantity: number;
  status?: string;
  createdByUserId: number;
  publicationDate?: string;
  language?: string;
  publisher?: string;
  coverType?: string;
  nbPages?: number;
  genre?: string;
  editorInChief?: string;
  issueNumber?: string;
  publicationFrequency?: string;
  issn?: string;
  releaseDate?: string;
  artists?: string;
  recordLabel?: string;
  discType?: string;
  director?: string;
  runtime?: number;
  studio?: string;
  subtitles?: string;
}

export type ProductFormValue = Omit<CreateProductRequest, 'createdByUserId'>;

export interface ProductDetail extends ProductFormValue {
  id: number;
}

export interface UpdateProductRequest extends ProductFormValue {
  updatedByUserId: number;
}

export interface ProductResponse {
  id: number;
  title: string;
  category: string;
  currentPrice: number;
  stockQuantity: number;
  status: string;
  message?: string;
}

export interface DeleteProductRequest {
  deletedByUserId: number;
}

/** Per UC Delete Product: stock=0 → deleted; stock>0 → deactivated */
export type DeleteProductOutcomeKind = 'deleted' | 'deactivated' | 'failed';

export interface DeleteProductOutcome {
  productId: number;
  kind: DeleteProductOutcomeKind;
  errorMessage?: string;
}

export interface DeleteProductsSummary {
  deletedCount: number;
  deactivatedCount: number;
  failed: DeleteProductOutcome[];
}

export interface ApiResponse<T> {
  code?: number;
  message?: string;
  result: T;
}
