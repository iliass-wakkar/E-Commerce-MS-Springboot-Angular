import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ProductRequestDTO {
    name: string;
    description: string;
    price: number;
    stockQuantity: number;
    imageUrl?: string;
    manufacturer: string;
    categoryId: number;
}

export interface ProductResponseDTO {
    id: number;
    name: string;
    // description is NOT in response
    price: number;
    stockQuantity: number;
    imageUrl?: string;
    manufacturer: string;
    productCategory: {
        id: number;
        name: string;
    };
    createdAt?: string;
    updatedAt?: string;
}

export interface CategoryDTO {
    id: number;
    name: string;
    description?: string;
}

@Injectable({
    providedIn: 'root'
})
export class ProductService {
    // Gateway URL: http://localhost:1111/PRODUCT-SERVICE/products
    private apiUrl = `${environment.apiUrl}/PRODUCT-SERVICE/products`;
    private categoryUrl = `${environment.apiUrl}/PRODUCT-SERVICE/categories`;

    constructor(private http: HttpClient) { }

    // Health check
    getServiceStatus(): Observable<string> {
        return this.http.get(`${this.apiUrl}/status`, { responseType: 'text' });
    }

    // Get all categories
    getCategories(): Observable<CategoryDTO[]> {
        return this.http.get<CategoryDTO[]>(this.categoryUrl);
    }

    // Create product
    createProduct(product: ProductRequestDTO): Observable<ProductResponseDTO> {
        return this.http.post<ProductResponseDTO>(this.apiUrl, product);
    }

    // Get all products
    getProducts(): Observable<ProductResponseDTO[]> {
        return this.http.get<ProductResponseDTO[]>(this.apiUrl);
    }

    // Get product by ID
    getProductById(id: number): Observable<ProductResponseDTO> {
        return this.http.get<ProductResponseDTO>(`${this.apiUrl}/${id}`);
    }

    // Update product
    updateProduct(id: number, product: ProductRequestDTO): Observable<ProductResponseDTO> {
        return this.http.put<ProductResponseDTO>(`${this.apiUrl}/${id}`, product);
    }

    // Delete product
    deleteProduct(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
