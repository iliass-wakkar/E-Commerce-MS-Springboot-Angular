import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  isAuthenticated$: Observable<boolean>;
  userRole$: Observable<string | null>;
  userName: string = 'John Doe';
  userInitials: string = '';
  cartCount$: Observable<number>;

  constructor(
    public authService: AuthService, 
    private cartService: CartService,
    private router: Router
  ) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
    this.userRole$ = this.authService.userRole$;
    
    // Subscribe to cart count changes
    this.cartCount$ = new Observable(observer => {
      this.cartService.cartItems$.subscribe(items => {
        const count = items.reduce((sum, item) => sum + item.quantity, 0);
        observer.next(count);
      });
    });
  }

  navigate(path: string): void {
    this.router.navigate([path]);
  }

  generateInitials(): void {
    const nameParts = this.userName.split(' ');
    const initials = nameParts
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');
    this.userInitials = initials;
  }

  logout(): void {
    this.authService.logout();
  }

  ngOnInit(): void {
    this.generateInitials();
  }
}
