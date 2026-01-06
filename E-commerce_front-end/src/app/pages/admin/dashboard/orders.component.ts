import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService, Order } from '../../../services/order.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <h2 class="text-3xl font-bold mb-6 text-gray-800">Gestion des Commandes</h2>

      <!-- Loading State -->
      <div *ngIf="loading" class="text-center py-8">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">Chargement des commandes...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <!-- Orders Table -->
      <div *ngIf="!loading && !error" class="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100">
        <table class="w-full text-left">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="p-4 font-semibold text-gray-600">ID</th>
              <th class="p-4 font-semibold text-gray-600">Client ID</th>
              <th class="p-4 font-semibold text-gray-600">Date</th>
              <th class="p-4 font-semibold text-gray-600">Total</th>
              <th class="p-4 font-semibold text-gray-600">Statut</th>
              <th class="p-4 font-semibold text-gray-600 text-right">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr *ngFor="let order of orders" class="hover:bg-gray-50 transition-colors">
              <td class="p-4 text-gray-600">#{{ order.id }}</td>
              <td class="p-4 text-gray-600">{{ order.userId || 'N/A' }}</td>
              <td class="p-4 text-gray-600">{{ order.orderDate | date:'short' }}</td>
              <td class="p-4 font-medium text-gray-800">{{ order.totalPrice | currency:'EUR' }}</td>
              <td class="p-4">
                <span [ngClass]="getStatusClass(order.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ order.status }}
                </span>
              </td>
              <td class="p-4 text-right space-x-2">
                <button (click)="viewDetails(order)" class="text-blue-600 hover:text-blue-800 font-medium text-sm">Détails</button>
                <button *ngIf="order.status === 'CREATED'" (click)="updateStatus(order, 'CONFIRMED')" class="text-green-600 hover:text-green-800 font-medium text-sm">Confirmer</button>
                <button *ngIf="order.status !== 'CANCELED'" (click)="updateStatus(order, 'CANCELED')" class="text-red-600 hover:text-red-800 font-medium text-sm">Annuler</button>
              </td>
            </tr>
            <tr *ngIf="orders.length === 0">
              <td colspan="6" class="p-8 text-center text-gray-500">Aucune commande trouvée.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Details Modal -->
    <div *ngIf="selectedOrder" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl mx-4 overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-gray-800">Commande #{{ selectedOrder.id }}</h3>
          <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>
        <div class="p-6">
            <div class="mb-4 grid grid-cols-2 gap-4">
                <div>
                    <p class="text-sm text-gray-500">Date</p>
                    <p class="font-medium">{{ selectedOrder.orderDate | date:'medium' }}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Total</p>
                    <p class="font-medium text-xl text-blue-600">{{ selectedOrder.totalPrice | currency:'EUR' }}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Client ID</p>
                    <p class="font-medium">{{ selectedOrder.userId || 'N/A' }}</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Statut</p>
                    <span [ngClass]="getStatusClass(selectedOrder.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                        {{ selectedOrder.status }}
                    </span>
                </div>
            </div>

            <h4 class="font-semibold mb-3">Articles</h4>
            <div class="bg-gray-50 rounded-lg p-4 max-h-60 overflow-y-auto">
                <div *ngFor="let item of selectedOrder.orderLineItems" class="flex justify-between items-center py-2 border-b border-gray-200 last:border-0">
                    <div>
                        <p class="font-medium">Produit #{{ item.productId }}</p>
                        <p class="text-sm text-gray-500">Quantité: {{ item.quantity }}</p>
                    </div>
                    <p class="font-medium">{{ item.price | currency:'EUR' }}</p>
                </div>
            </div>
        </div>
        <div class="p-6 bg-gray-50 flex justify-end">
            <button (click)="closeModal()" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium">Fermer</button>
        </div>
      </div>
    </div>
  `
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  error: string | null = null;
  selectedOrder: Order | null = null;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;
    this.orderService.getAllOrders().subscribe({
      next: (data: Order[]) => {
        this.orders = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading orders', err);
        this.error = 'Impossible de charger les commandes.';
        this.loading = false;
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED': return 'bg-green-100 text-green-700';
      case 'CANCELED': return 'bg-red-100 text-red-700';
      default: return 'bg-blue-100 text-blue-700';
    }
  }

  updateStatus(order: Order, status: string) {
    if (confirm(`Êtes-vous sûr de vouloir passer la commande #${order.id} au statut ${status} ?`)) {
      this.orderService.updateOrderStatus(order.id, status).subscribe({
        next: (updatedOrder: Order) => {
          // Update local state
          const index = this.orders.findIndex(o => o.id === order.id);
          if (index !== -1) {
            this.orders[index] = updatedOrder;
          }
          if (this.selectedOrder && this.selectedOrder.id === order.id) {
            this.selectedOrder = updatedOrder;
          }
        },
        error: (err: any) => alert('Erreur lors de la mise à jour du statut')
      });
    }
  }

  viewDetails(order: Order) {
    this.selectedOrder = order;
  }

  closeModal() {
    this.selectedOrder = null;
  }
}