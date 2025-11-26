import { Component } from '@angular/core';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [],
  template: `
    <h2 class="text-3xl font-bold mb-6 text-gray-800">Gestion des Commandes</h2>
    <p class="text-gray-600">Ici, vous pourrez suivre et gérer toutes les commandes.</p>
  `
})
export class OrdersComponent { }