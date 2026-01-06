import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductService, ProductResponseDTO } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  products: ProductResponseDTO[] = [];
  loading = true;
  error: string | null = null;
  addedToCartId: number | null = null;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) { }

  viewProductDetail(productId: number): void {
    this.router.navigate(['/products', productId]);
  }

  addToCart(product: ProductResponseDTO, event: Event): void {
    event.stopPropagation();
    
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.cartService.addToCart(product, 1);
    
    // Show brief feedback
    this.addedToCartId = product.id;
    setTimeout(() => {
      this.addedToCartId = null;
    }, 1500);
  }

  isInCart(productId: number): boolean {
    return this.cartService.isInCart(productId);
  }

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (data: any) => {
        this.products = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading products', err);
        this.error = 'Failed to load products';
        this.loading = false;
      }
    });
  }
}
