import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService, ProductResponseDTO } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';

interface ProductDetailView extends ProductResponseDTO {
  description: string;
  gallery: string[];
  highlights: string[];
  specs: { label: string; value: string }[];
  rating: number;
  reviewsCount: number;
  shipping: string;
  returnPolicy: string;
  sku: string;
}

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  productId: number | null = null;
  selectedImage: string = '';
  loading = false;
  error: string | null = null;
  addedToCart = false;

  product: ProductDetailView | null = null;
  staticProduct: ProductDetailView = {
    id: 101,
    name: 'Aurora Wireless Headphones',
    description:
      'Premium over-ear wireless headphones with hybrid ANC, studio-grade drivers, and 40 hours of battery life. Crafted for comfort with a breathable mesh headband and plush ear cushions.',
    price: 249.99,
    stockQuantity: 18,
    imageUrl: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=1200&q=80',
    manufacturer: 'NovaSound Labs',
    productCategory: {
      id: 12,
      name: 'Audio'
    },
    createdAt: undefined,
    updatedAt: undefined,
    gallery: [
      'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=1200&q=80'
    ],
    highlights: [
      'Hybrid Active Noise Cancellation (up to 35dB)',
      'Hi-Res certified 40mm drivers',
      'Bluetooth 5.3 with multipoint pairing',
      '40h battery life (30h with ANC)',
      'USB-C fast charging (10 min = 5h playback)'
    ],
    specs: [
      { label: 'Driver Size', value: '40mm beryllium-coated' },
      { label: 'Frequency Response', value: '10Hz - 40kHz' },
      { label: 'Codec Support', value: 'AAC, SBC, aptX Adaptive' },
      { label: 'Weight', value: '265g' },
      { label: 'Battery', value: '1000mAh / up to 40h' },
      { label: 'Charging', value: 'USB-C, 0-100% in 90m' }
    ],
    rating: 4.8,
    reviewsCount: 284,
    shipping: 'Free express shipping (2-4 days)',
    returnPolicy: '30-day hassle-free returns',
    sku: 'NS-AURORA-101'
  };

  constructor(
    private route: ActivatedRoute, 
    private productService: ProductService,
    private cartService: CartService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.productId = params['id'];
      if (this.productId) {
        this.loadProduct(this.productId);
      }
    });
  }

  loadProduct(productId: number): void {
    this.loading = true;
    this.error = null;
    this.productService.getProductById(productId).subscribe({
      next: (product: ProductResponseDTO) => {
        // Map ProductResponseDTO to ProductDetailView with enhanced data
        this.product = {
          ...product,
          description: `Premium ${product.name} from ${product.manufacturer}. High quality and reliable performance.`,
          gallery: [
            product.imageUrl || 'https://via.placeholder.com/400',
            'https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80',
            'https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=1200&q=80'
          ],
          highlights: [
            `Premium quality from ${product.manufacturer}`,
            `Stock available: ${product.stockQuantity} units`,
            'Fast delivery',
            'Easy returns'
          ],
          specs: [
            { label: 'Manufacturer', value: product.manufacturer },
            { label: 'Category', value: product.productCategory?.name || 'N/A' },
            { label: 'Price', value: `$${product.price.toFixed(2)}` },
            { label: 'In Stock', value: product.stockQuantity > 0 ? 'Yes' : 'No' }
          ],
          rating: 4.5,
          reviewsCount: 0,
          shipping: 'Free express shipping (2-4 days)',
          returnPolicy: '30-day hassle-free returns',
          sku: `SKU-${product.id}`
        };
        if (this.product && this.product.gallery.length > 0) {
          this.selectedImage = this.product.gallery[0];
        }
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Failed to load product:', err);
        this.error = 'Failed to load product details';
        this.loading = false;
      }
    });
  }

  setImage(image: string): void {
    this.selectedImage = image;
  }

  addToCart(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.product) {
      this.cartService.addToCart(this.product, 1);
      this.addedToCart = true;
      setTimeout(() => {
        this.addedToCart = false;
      }, 2000);
    }
  }

  buyNow(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.product) {
      this.cartService.addToCart(this.product, 1);
      this.router.navigate(['/orders']);
    }
  }

  isInCart(): boolean {
    return this.product ? this.cartService.isInCart(this.product.id) : false;
  }
}

// Keep static product for reference/comments
const STATIC_PRODUCT_REFERENCE = {
  id: 101,
  name: 'Aurora Wireless Headphones',
  description:
    'Premium over-ear wireless headphones with hybrid ANC, studio-grade drivers, and 40 hours of battery life. Crafted for comfort with a breathable mesh headband and plush ear cushions.',
  price: 249.99,
  stockQuantity: 18,
  imageUrl: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=1200&q=80',
  manufacturer: 'NovaSound Labs',
  productCategory: {
    id: 12,
    name: 'Audio'
  },
  createdAt: undefined,
  updatedAt: undefined,
  gallery: [
    'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=1200&q=80'
  ],
  highlights: [
    'Hybrid Active Noise Cancellation (up to 35dB)',
    'Hi-Res certified 40mm drivers',
    'Bluetooth 5.3 with multipoint pairing',
    '40h battery life (30h with ANC)',
    'USB-C fast charging (10 min = 5h playback)'
  ],
  specs: [
    { label: 'Driver Size', value: '40mm beryllium-coated' },
    { label: 'Frequency Response', value: '10Hz - 40kHz' },
    { label: 'Codec Support', value: 'AAC, SBC, aptX Adaptive' },
    { label: 'Weight', value: '265g' },
    { label: 'Battery', value: '1000mAh / up to 40h' },
    { label: 'Charging', value: 'USB-C, 0-100% in 90m' }
  ],
  rating: 4.8,
  reviewsCount: 284,
  shipping: 'Free express shipping (2-4 days)',
  returnPolicy: '30-day hassle-free returns',
  sku: 'NS-AURORA-101'
};
