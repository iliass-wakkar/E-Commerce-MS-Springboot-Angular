import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

// Request DTOs expected by order-service (port 8082, /api/orders)
export interface OrderLineItemRequest {
  productId: number;
  quantity: number;
}

export interface OrderRequest {
  orderLineItemsDtoList: OrderLineItemRequest[];
}

// Response DTOs returned by order-service
export type OrderStatus = 'CREATED' | 'CONFIRMED' | 'CANCELED';

export interface OrderLineItem {
  id: number;
  productId: number;
  quantity: number;
  price: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  totalPrice: number;
  orderDate: string; // ISO-8601 datetime string
  status: OrderStatus;
  userId?: number;
  orderLineItems: OrderLineItem[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  // Gateway URL
  private apiUrl = `${environment.apiUrl}/COMMANDE-SERVICE/api/orders`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new order
   * Success: 201 CREATED with full Order payload
   * Errors: 400 (empty cart / unavailable product / insufficient stock), 500 (server)
   */
  createOrder(orderRequest: OrderRequest): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, orderRequest).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get a single order by id
   * Errors: 404 (not found), 500 (server)
   */
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get all orders (useful for history views)
   */
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update order status (Admin)
   */
  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${id}/status`, status).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Order service error. Please try again.';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 400:
          errorMessage = 'Cart validation failed (empty cart, unavailable product, or insufficient stock).';
          break;
        case 404:
          errorMessage = 'Order not found.';
          break;
        case 500:
          errorMessage = 'Order service unavailable. Please try again later.';
          break;
        default:
          errorMessage = `Error Code: ${error.status} - ${error.message}`;
      }
    }

    console.error('Order Service Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
