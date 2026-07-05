export interface Product {
  id: number;
  title: string;
  category: string;
  generalDescription: string;
  barcode: string;
  imageUrl: string;
  originalValue: number;
  currentPrice: number;
  weight: number;
  length: number;
}

export interface ProductSearchResponse {
  id: number;
  title: string;
  category: string;
  currentPrice: number;
  imageUrl: string;
  originalValue: number;
  stockQuantity: number;
  status: string;
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CartItem extends OrderItemRequest {
  product: Product;
}

export interface DeliveryInfoRequest {
  receiverName: string;
  email: string;
  phoneNumber: string;
  address: string;
  city: string;
  instruction?: string;
  expectedDate?: string;
}

export interface PlaceOrderRequest {
  items: OrderItemRequest[];
  deliveryInfo: DeliveryInfoRequest;
}

export interface ShippingFeeRequest {
  items: OrderItemRequest[];
  address: string;
  city: string;
}

export interface UnavailableItemDetail {
  productId: number;
  productTitle?: string;
  requestedQuantity?: number;
  availableQuantity: number;
}

export interface StockValidationResponse {
  valid: boolean;
  unavailableItems: UnavailableItemDetail[];
}

export interface ShippingFeeResponse {
  shippingFee: number;
  freeShippingApplied: boolean;
}

export interface PlaceOrderResponse {
  orderId: number;
  customerName: string;
  phoneNumber: string;
  shippingAddress: string;
  city: string;
  orderStatus: string;
  subTotal: number;
  vatAmount: number;
  shippingFee: number;
  totalAmount: number;
  freeShippingApplied: boolean;
  orderItems: OrderItemDetail[];
}

export interface OrderItemDetail {
  productId: number;
  productTitle: string;
  quantity: number;
  price: number;
  itemTotal: number;
}

export interface PayOrderResponse {
  orderId: number;
  customerName: string;
  email: string;
  phoneNumber: string;
  shippingAddress: string;
  city: string;
  totalAmount: number;
  orderStatus: string;
  transactionId: number;
  transactionContent: string;
  transactionDatetime: string;
  paymentMethod: string;
}

export interface ManagerOrderItemResponse {
  productId: number;
  productTitle: string;
  imageUrl?: string;
  quantity: number;
  availableQuantity: number;
  price: number;
  itemTotal: number;
}

export interface ManagerOrderResponse {
  orderId: number;
  customerName: string;
  email: string;
  phoneNumber: string;
  shippingAddress: string;
  city: string;
  totalAmount: number;
  orderStatus: string;
  createdDate: string;
  paymentMethod?: string;
  transactionId?: number;
  transactionStatus?: string;
  refundStatus?: string;
  refundType?: string;
  refundNote?: string;
  orderItems: ManagerOrderItemResponse[];
}

export interface ManagerOrderPageResponse {
  orders: ManagerOrderResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApproveOrderRequest {
  managerUserId: number;
}

export interface RejectOrderRequest {
  managerUserId: number;
  reason?: string;
}

export interface ApiResponse<T> {
  code?: number;
  message?: string;
  result: T;
}
