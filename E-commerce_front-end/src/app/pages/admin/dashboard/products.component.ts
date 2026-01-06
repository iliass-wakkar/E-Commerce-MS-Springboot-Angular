import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService, ProductResponseDTO, ProductRequestDTO, CategoryDTO } from '../../../services/product.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-3xl font-bold text-gray-800">Gestion des Produits</h2>
        <button (click)="openModal()" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2">
          <span class="text-xl">+</span> Ajouter un produit
        </button>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="text-center py-8">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">Chargement des produits...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <!-- Products Table -->
      <div *ngIf="!loading && !error" class="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100">
        <table class="w-full text-left">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="p-4 font-semibold text-gray-600">ID</th>
              <th class="p-4 font-semibold text-gray-600">Image</th>
              <th class="p-4 font-semibold text-gray-600">Nom</th>
              <th class="p-4 font-semibold text-gray-600">Prix</th>
              <th class="p-4 font-semibold text-gray-600">Stock</th>
              <th class="p-4 font-semibold text-gray-600">Fabricant</th>
              <th class="p-4 font-semibold text-gray-600">Catégorie</th>
              <th class="p-4 font-semibold text-gray-600 text-right">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr *ngFor="let product of products" class="hover:bg-gray-50 transition-colors">
              <td class="p-4 text-gray-600">#{{ product.id }}</td>
              <td class="p-4">
                <img [src]="product.imageUrl || 'assets/placeholder.png'" alt="Product" class="w-10 h-10 rounded object-cover bg-gray-100">
              </td>
              <td class="p-4 font-medium text-gray-800">{{ product.name }}</td>
              <td class="p-4 text-gray-800 font-medium">{{ product.price | currency:'EUR' }}</td>
              <td class="p-4">
                <span [class]="product.stockQuantity > 0 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ product.stockQuantity }} en stock
                </span>
              </td>
              <td class="p-4 text-gray-600">{{ product.manufacturer }}</td>
              <td class="p-4 text-gray-600">
                <span class="bg-blue-50 text-blue-700 px-2 py-1 rounded-lg text-xs">
                  {{ product.productCategory.name || 'N/A' }}
                </span>
              </td>
              <td class="p-4 text-right space-x-2">
                <button (click)="openModal(product)" class="text-blue-600 hover:text-blue-800 font-medium text-sm">Modifier</button>
                <button (click)="deleteProduct(product.id)" class="text-red-600 hover:text-red-800 font-medium text-sm">Supprimer</button>
              </td>
            </tr>
            <tr *ngIf="products.length === 0">
              <td colspan="8" class="p-8 text-center text-gray-500">Aucun produit trouvé.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div *ngIf="isModalOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-gray-800">{{ isEditing ? 'Modifier' : 'Ajouter' }} un produit</h3>
          <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>
        
        <form [formGroup]="productForm" (ngSubmit)="saveProduct()" class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nom</label>
            <input formControlName="name" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all">
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description <span class="text-xs text-gray-500">(Non visible après création)</span></label>
            <textarea formControlName="description" rows="3" placeholder="Entrer une description..." class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"></textarea>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
            <input formControlName="imageUrl" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all">
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Fabricant</label>
            <input formControlName="manufacturer" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all">
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Catégorie</label>
            <select formControlName="categoryId" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all bg-white">
              <option [ngValue]="null" disabled>Sélectionner une catégorie</option>
              <option *ngFor="let cat of categories" [value]="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Prix</label>
              <input formControlName="price" type="number" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Stock</label>
              <input formControlName="stockQuantity" type="number" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all">
            </div>
          </div>

          <div class="flex justify-end gap-3 mt-6">
            <button type="button" (click)="closeModal()" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition-colors">Annuler</button>
            <button type="submit" [disabled]="productForm.invalid || submitting" class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              {{ submitting ? 'Enregistrement...' : 'Enregistrer' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class ProductsComponent implements OnInit {
  products: ProductResponseDTO[] = [];
  categories: CategoryDTO[] = [];
  loading = true;
  error: string | null = null;

  isModalOpen = false;
  isEditing = false;
  submitting = false;
  currentProductId: number | null = null;

  productForm: FormGroup;

  constructor(
    private productService: ProductService,
    private fb: FormBuilder
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required]],
      imageUrl: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      manufacturer: ['', [Validators.required]],
      categoryId: [null, [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading products', err);
        this.error = 'Impossible de charger les produits.';
        this.loading = false;
      }
    });
  }

  openModal(product?: ProductResponseDTO): void {
    this.isModalOpen = true;
    if (product) {
      this.isEditing = true;
      this.currentProductId = product.id;
      this.productForm.patchValue({
        name: product.name,
        // description is not available in response, so we leave it blank or user must re-enter
        description: '',
        imageUrl: product.imageUrl,
        price: product.price,
        stockQuantity: product.stockQuantity,
        manufacturer: product.manufacturer,
        categoryId: product.productCategory?.id
      });
    } else {
      this.isEditing = false;
      this.currentProductId = null;
      this.productForm.reset({ price: 0, stockQuantity: 0 });
    }
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.productForm.reset();
  }

  saveProduct(): void {
    if (this.productForm.invalid) return;

    this.submitting = true;
    const productData: ProductRequestDTO = this.productForm.value;

    const request = this.isEditing && this.currentProductId
      ? this.productService.updateProduct(this.currentProductId, productData)
      : this.productService.createProduct(productData);

    request.subscribe({
      next: () => {
        this.loadProducts();
        this.closeModal();
        this.submitting = false;
      },
      error: (err) => {
        console.error('Error saving product', err);
        alert('Erreur lors de l\'enregistrement du produit');
        this.submitting = false;
      }
    });
  }

  deleteProduct(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          this.loadProducts();
        },
        error: (err) => {
          console.error('Error deleting product', err);
          alert('Impossible de supprimer le produit');
        }
      });
    }
  }
}