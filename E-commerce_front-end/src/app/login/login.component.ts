import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  authError = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    // Si l'utilisateur est déjà connecté, le rediriger vers la page d'accueil
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
      return; // Arrêter l'exécution pour ne pas initialiser le formulaire inutilement
    }
  }

  onSubmit(): void {
    this.authError = false;
    if (this.loginForm.invalid) {
      return;
    }

    const email = this.loginForm.value.email;
    const password = this.loginForm.value.password;

    this.authService.login({ email, password }).subscribe({
      next: () => {
        console.log('Login successful, redirecting to /home...');
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Login failed', err);
        this.authError = true;
      }
    });
  }
}
