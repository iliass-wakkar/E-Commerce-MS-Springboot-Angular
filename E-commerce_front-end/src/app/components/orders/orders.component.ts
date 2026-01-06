import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { OrderService, Order, OrderRequest, OrderStatus } from '../../services/order.service';
import { ProductService, ProductResponseDTO } from '../../services/product.service';
import { CartService, CartItem } from '../../services/cart.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  // Cart state
  cartItems: CartItem[] = [];
  showCart = true;
  selectedTab: 'cart' | 'products' | 'orders' = 'cart';

  // Products list
  availableProducts: ProductResponseDTO[] = [];
  loadingProducts = false;

  // Orders list
  orders: Order[] = [];
  loadingOrders = false;

  // Modal states
  showAddProductModal = false;
  showOrderDetailsModal = false;
  selectedOrder: Order | null = null;
  selectedProduct: ProductResponseDTO | null = null;

  // Forms
  addToCartForm!: FormGroup;
  orderLoading = false;
  orderSuccess = false;
  orderError: string | null = null;

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private cartService: CartService,
    private fb: FormBuilder
  ) {
    this.addToCartForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    // Subscribe to cart from service
    this.cartService.cartItems$.subscribe(items => {
      this.cartItems = items;
    });
    
    this.loadProducts();
    this.loadOrders();
  }

  // ==================== CART OPERATIONS ====================

  get cartTotal(): number {
    return this.cartService.getCartTotal();
  }

  get cartItemCount(): number {
    return this.cartService.getCartItemCount();
  }

  addToCart(product: ProductResponseDTO): void {
    this.cartService.addToCart(product, 1);
    this.showAddProductModal = false;
    this.selectedProduct = null;
  }

  updateCartItemQuantity(cartItem: CartItem, newQuantity: number): void {
    this.cartService.updateCartItemQuantity(cartItem.product.id, newQuantity);
  }

  removeFromCart(cartItem: CartItem): void {
    this.cartService.removeFromCart(cartItem.product.id);
  }

  clearCart(): void {
    if (confirm('Are you sure you want to clear the cart?')) {
      this.cartService.clearCart();
      this.orderSuccess = false;
      this.orderError = null;
    }
  }

  // ==================== PRODUCT OPERATIONS ====================

  loadProducts(): void {
    this.loadingProducts = true;
    this.productService.getProducts().subscribe({
      next: (products: ProductResponseDTO[]) => {
        this.availableProducts = products;
        this.loadingProducts = false;
      },
      error: (err: unknown) => {
        console.error('Failed to load products:', err);
        this.loadingProducts = false;
      }
    });
  }

  openAddProductModal(product: ProductResponseDTO): void {
    this.selectedProduct = product;
    this.addToCartForm.reset({ quantity: 1 });
    this.showAddProductModal = true;
  }

  closeAddProductModal(): void {
    this.showAddProductModal = false;
    this.selectedProduct = null;
    this.addToCartForm.reset({ quantity: 1 });
  }

  confirmAddToCart(): void {
    if (this.addToCartForm.valid && this.selectedProduct) {
      const quantity = this.addToCartForm.value.quantity;
      this.cartService.addToCart(this.selectedProduct, quantity);
      this.closeAddProductModal();
    }
  }

  // ==================== ORDER OPERATIONS ====================

  loadOrders(): void {
    this.loadingOrders = true;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        console.log('Orders loaded:', orders);
        this.orders = orders;
        this.loadingOrders = false;
      },
      error: (err) => {
        console.error('Failed to load orders:', err);
        this.loadingOrders = false;
      }
    });
  }

  placeOrder(): void {
    if (this.cartItems.length === 0) {
      alert('Cart is empty. Add products before placing an order.');
      return;
    }

    this.orderLoading = true;
    this.orderError = null;
    this.orderSuccess = false;

    const orderRequest: OrderRequest = {
      orderLineItemsDtoList: this.cartItems.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }))
    };

    console.log('Placing order with request:', orderRequest);

    this.orderService.createOrder(orderRequest).subscribe({
      next: (order) => {
        console.log('Order placed successfully:', order);
        this.orderSuccess = true;
        this.orders.unshift(order); // Add to top of orders list
        this.cartService.clearCart(); // Clear cart via service
        this.orderLoading = false;
        setTimeout(() => {
          this.orderSuccess = false;
        }, 5000);
      },
      error: (err: unknown) => {
        console.error('Order placement failed:', err);
        this.orderError = err instanceof Error ? err.message : 'Failed to place order';
        this.orderLoading = false;
      }
    });
  }

  viewOrderDetails(order: Order): void {
    this.selectedOrder = order;
    this.showOrderDetailsModal = true;
  }

  closeOrderDetailsModal(): void {
    this.showOrderDetailsModal = false;
    this.selectedOrder = null;
  }

  getStatusBadgeClass(status: OrderStatus): string {
    switch (status) {
      case 'CREATED':
        return 'badge-yellow';
      case 'CONFIRMED':
        return 'badge-blue';
      case 'CANCELED':
        return 'badge-red';
      default:
        return 'badge-gray';
    }
  }
}
