import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  isAuthenticated$: Observable<boolean>;
  userRole$: Observable<string | null>;
  userName: string = 'John Doe'; // Placeholder for now
  userInitials: string = '';

  constructor(public authService: AuthService, private router: Router) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
    this.userRole$ = this.authService.userRole$;
  }

  ngOnInit(): void {
    this.generateInitials();
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
}
