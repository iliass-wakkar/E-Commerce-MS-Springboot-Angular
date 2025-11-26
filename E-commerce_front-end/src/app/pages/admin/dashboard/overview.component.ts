import { Component } from '@angular/core';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [],
  template: `<!-- Page Header -->
<h2 class="text-3xl font-bold text-gray-900 mb-8">Dashboard Overview</h2>

<!-- KPI Cards Grid -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
  <!-- Card 1: Total Clients -->
  <div class="bg-white rounded-xl shadow-lg p-6 flex items-center gap-5">
    <div class="w-12 h-12 rounded-full flex items-center justify-center bg-blue-100 text-blue-600">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.653-.124-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.653.124-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
      </svg>
    </div>
    <div>
      <p class="text-3xl font-bold text-gray-900">1,452</p>
      <p class="text-sm text-gray-500">Clients Total</p>
    </div>
  </div>

  <!-- Card 2: Orders (30 days) -->
  <div class="bg-white rounded-xl shadow-lg p-6 flex items-center gap-5">
    <div class="w-12 h-12 rounded-full flex items-center justify-center bg-green-100 text-green-600">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path>
      </svg>
    </div>
    <div>
      <p class="text-3xl font-bold text-gray-900">389</p>
      <p class="text-sm text-gray-500">Commandes (30j)</p>
    </div>
  </div>

  <!-- Card 3: Revenue (30 days) -->
  <div class="bg-white rounded-xl shadow-lg p-6 flex items-center gap-5">
    <div class="w-12 h-12 rounded-full flex items-center justify-center bg-yellow-100 text-yellow-600">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8C9.791 8 8 9.791 8 12s1.791 4 4 4 4-1.791 4-4-1.791-4-4-4z"></path>
      </svg>
    </div>
    <div>
      <p class="text-3xl font-bold text-gray-900">12,450 €</p>
      <p class="text-sm text-gray-500">Revenu (30j)</p>
    </div>
  </div>

  <!-- Card 4: Active Products -->
  <div class="bg-white rounded-xl shadow-lg p-6 flex items-center gap-5">
    <div class="w-12 h-12 rounded-full flex items-center justify-center bg-red-100 text-red-600">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5a2 2 0 012 2v5a2 2 0 01-2 2H7a2 2 0 01-2-2V5a2 2 0 012-2zm0 0v11m0-11h11"></path>
      </svg>
    </div>
    <div>
      <p class="text-3xl font-bold text-gray-900">217</p>
      <p class="text-sm text-gray-500">Produits Actifs</p>
    </div>
  </div>
</div>

<!-- Recent Activities Section -->
<div class="mt-12 grid grid-cols-1 lg:grid-cols-2 gap-8">
  <!-- Table 1: Latest Orders -->
  <div class="bg-white rounded-xl shadow-lg overflow-hidden">
    <h3 class="p-4 border-b text-lg font-semibold text-gray-800">Dernières Commandes</h3>
    <table class="w-full">
      <thead class="bg-gray-50">
        <tr>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Commande</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Client</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
        <tr>
          <td class="p-3 text-sm text-gray-700">#1024</td>
          <td class="p-3 text-sm text-gray-700">Jean Dupont</td>
          <td class="p-3 text-sm text-gray-700">128.50 €</td>
          <td class="p-3 text-sm text-gray-700"><span class="bg-green-100 text-green-800 text-xs font-medium px-2.5 py-0.5 rounded-full">Livré</span></td>
        </tr>
        <tr>
          <td class="p-3 text-sm text-gray-700">#1023</td>
          <td class="p-3 text-sm text-gray-700">Marie Claire</td>
          <td class="p-3 text-sm text-gray-700">45.00 €</td>
          <td class="p-3 text-sm text-gray-700"><span class="bg-yellow-100 text-yellow-800 text-xs font-medium px-2.5 py-0.5 rounded-full">En attente</span></td>
        </tr>
        <tr>
          <td class="p-3 text-sm text-gray-700">#1022</td>
          <td class="p-3 text-sm text-gray-700">Philippe Leroy</td>
          <td class="p-3 text-sm text-gray-700">210.00 €</td>
          <td class="p-3 text-sm text-gray-700"><span class="bg-red-100 text-red-800 text-xs font-medium px-2.5 py-0.5 rounded-full">Annulé</span></td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- Table 2: New Customers -->
  <div class="bg-white rounded-xl shadow-lg overflow-hidden">
    <h3 class="p-4 border-b text-lg font-semibold text-gray-800">Nouveaux Clients</h3>
    <table class="w-full">
      <thead class="bg-gray-50">
        <tr>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date d'inscription</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
        <tr>
          <td class="p-3 text-sm text-gray-700">Alice Martin</td>
          <td class="p-3 text-sm text-gray-700">alice.m@example.com</td>
          <td class="p-3 text-sm text-gray-700">2023-10-26</td>
        </tr>
        <tr>
          <td class="p-3 text-sm text-gray-700">Bob Garcia</td>
          <td class="p-3 text-sm text-gray-700">bob.g@example.com</td>
          <td class="p-3 text-sm text-gray-700">2023-10-25</td>
        </tr>
        <tr>
          <td class="p-3 text-sm text-gray-700">Carla Simon</td>
          <td class="p-3 text-sm text-gray-700">carla.s@example.com</td>
          <td class="p-3 text-sm text-gray-700">2023-10-24</td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
`
})
export class OverviewComponent { }