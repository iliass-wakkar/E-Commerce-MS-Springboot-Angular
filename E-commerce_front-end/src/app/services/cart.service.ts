import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { ProductResponseDTO } from './product.service';
import { environment } from '../../environments/environment';
import { isPlatformBrowser } from '@angular/common';

export interface CartItem {
  product: ProductResponseDTO;
  quantity: number;
  subtotal: number;
}

interface BackendCartItemResponse {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  price: number;
  quantity: number;
  subtotal: number;
}

interface BackendCartResponse {
  id: number;
  userId: number;
  items: BackendCartItemResponse[];
  totalPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  public cartItems$: Observable<CartItem[]> = this.cartItemsSubject.asObservable();
  private apiUrl = `${environment.apiUrl}/COMMANDE-SERVICE/api/cart`;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // Cart will be loaded after login, not on init
  }

  private isLoggedIn(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('accessToken');
    }
    return false;
  }

  loadCart(): void {
    if (!this.isLoggedIn()) {
      this.cartItemsSubject.next([]);
      return;
    }
    this.http.get<BackendCartResponse>(this.apiUrl).subscribe({
      next: (response) => {
        const items = response.items.map(item => this.mapBackendItemToCartItem(item));
        this.cartItemsSubject.next(items);
      },
      error: (err) => {
        console.error('Failed to load cart from backend:', err);
        // If 401 or error, cart is empty
        this.cartItemsSubject.next([]);
      }
    });
  }

  // Clear cart from memory only (used on logout)
  clearLocalCart(): void {
    this.cartItemsSubject.next([]);
  }

  addToCart(product: ProductResponseDTO, quantity: number = 1): void {
    if (!this.isLoggedIn()) {
      console.warn('Cannot add to cart: user not logged in');
      return;
    }
    this.http.post<BackendCartResponse>(`${this.apiUrl}/items`, null, {
      params: {
        productId: product.id.toString(),
        quantity: quantity.toString()
      }
    }).subscribe({
      next: (response) => {
        const items = response.items.map(item => this.mapBackendItemToCartItem(item));
        this.cartItemsSubject.next(items);
      },
      error: (err) => console.error('Failed to add to cart:', err)
    });
  }

  removeFromCart(productId: number): void {
    if (!this.isLoggedIn()) {
      return;
    }
    this.http.delete<BackendCartResponse>(`${this.apiUrl}/items/${productId}`).subscribe({
      next: (response) => {
        const items = response.items.map(item => this.mapBackendItemToCartItem(item));
        this.cartItemsSubject.next(items);
      },
      error: (err) => console.error('Failed to remove from cart:', err)
    });
  }

  clearCart(): void {
    if (!this.isLoggedIn()) {
      this.cartItemsSubject.next([]);
      return;
    }
    this.http.delete<void>(this.apiUrl).subscribe({
      next: () => {
        this.cartItemsSubject.next([]);
      },
      error: (err) => console.error('Failed to clear cart:', err)
    });
  }

  updateCartItemQuantity(productId: number, newQuantity: number): void {
    if (!this.isLoggedIn()) {
      return;
    }
    this.http.put<BackendCartResponse>(`${this.apiUrl}/items`, null, {
      params: {
        productId: productId.toString(),
        quantity: newQuantity.toString()
      }
    }).subscribe({
      next: (response) => {
        const items = response.items.map(item => this.mapBackendItemToCartItem(item));
        this.cartItemsSubject.next(items);
      },
      error: (err) => console.error('Failed to update cart item:', err)
    });
  }

  private mapBackendItemToCartItem(backendItem: BackendCartItemResponse): CartItem {
    return {
      product: {
        id: backendItem.productId,
        name: backendItem.productName,
        price: backendItem.price,
        imageUrl: backendItem.productImageUrl,
        stockQuantity: 0, // Not returned by cart API
        manufacturer: '', // Not returned
        productCategory: { id: 0, name: '' } // Not returned
      },
      quantity: backendItem.quantity,
      subtotal: backendItem.subtotal
    };
  }

  getCartItems(): CartItem[] {
    return this.cartItemsSubject.value;
  }

  getCartItemCount(): number {
    return this.cartItemsSubject.value.reduce((sum, item) => sum + item.quantity, 0);
  }

  getCartTotal(): number {
    return this.cartItemsSubject.value.reduce((sum, item) => sum + item.subtotal, 0);
  }

  isInCart(productId: number): boolean {
    return this.cartItemsSubject.value.some(item => item.product.id === productId);
  }

  getCartItemQuantity(productId: number): number {
    const item = this.cartItemsSubject.value.find(i => i.product.id === productId);
    return item ? item.quantity : 0;
  }
}
