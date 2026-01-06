import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class AdminProfileComponent implements OnInit {
  user: any = null;
  loading = true;
  error: string | null = null;

  // Modal states
  isEditModalOpen = false;
  isDeleteConfirmOpen = false;
  editForm!: FormGroup;

  // Form control accessors for template
  firstNameControl: any;
  lastNameControl: any;
  emailControl: any;
  phoneControl: any;
  shippingAddressControl: any;

  get initials(): string {
    if (!this.user?.firstName || !this.user?.lastName) return '';
    return (this.user.firstName.charAt(0) + this.user.lastName.charAt(0)).toUpperCase();
  }

  constructor(
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.editForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', []],
      shippingAddress: ['', []]
    });
  }

  ngOnInit(): void {
    this.loadUserProfile();
    this.initializeFormControls();
  }

  private initializeFormControls(): void {
    this.firstNameControl = this.editForm.get('firstName');
    this.lastNameControl = this.editForm.get('lastName');
    this.emailControl = this.editForm.get('email');
    this.phoneControl = this.editForm.get('phone');
    this.shippingAddressControl = this.editForm.get('shippingAddress');
  }

  loadUserProfile(): void {
    this.loading = true;
    this.error = null;
    this.authService.getAuthenticatedUser().subscribe({
      next: (userData: any) => {
        this.user = userData;
        this.loading = false;
        // Populate form with user data
        this.editForm.patchValue({
          firstName: userData.firstName,
          lastName: userData.lastName,
          email: userData.email,
          phone: userData.phone || '',
          shippingAddress: userData.shippingAddress || ''
        });
      },
      error: (err: any) => {
        this.error = 'Failed to load user profile';
        console.error('Error loading profile:', err);
        this.loading = false;
      }
    });
  }

  openEditModal(): void {
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
  }

  saveProfile(): void {
    if (this.editForm.invalid) return;

    const updatedData = this.editForm.value;
    this.authService.updateProfile(updatedData).subscribe({
      next: (response: any) => {
        this.user = response;
        this.isEditModalOpen = false;
        alert('Profile updated successfully!');
      },
      error: (err: any) => {
        console.error('Error updating profile:', err);
        alert('Failed to update profile');
      }
    });
  }

  openDeleteConfirm(): void {
    this.isDeleteConfirmOpen = true;
  }

  closeDeleteConfirm(): void {
    this.isDeleteConfirmOpen = false;
  }

  confirmDelete(): void {
    this.authService.deleteProfile().subscribe({
      next: () => {
        alert('Account deleted successfully');
        // Logout and redirect
        this.authService.logout();
      },
      error: (err: any) => {
        console.error('Error deleting profile:', err);
        alert('Failed to delete account');
        this.isDeleteConfirmOpen = false;
      }
    });
  }
}

