import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientFormComponent } from './client-form.component';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule, ClientFormComponent],
  templateUrl: './clients.component.html',
})
export class ClientsComponent implements OnInit {
  clients: any[] = [];
  filteredClients: any[] = [];
  searchTerm: string = '';
  selectedFilter: string = 'all'; // Pour les filtres futurs (ex: actifs, inactifs)

  isModalOpen = false;
  currentClient: any | null = null;

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.userService.getUsers().subscribe({
      next: (data: any) => {
        this.clients = data;
        this.filteredClients = [...this.clients];
        console.log('Clients loaded:', this.clients);
      },
      error: (err: any) => console.error('Failed to load clients', err)
    });
  }

  onSearch(): void {
    this.filteredClients = this.clients.filter(client =>
      client.firstName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      client.lastName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      client.email.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  applyFilter(): void {
    // La logique de filtrage sera implémentée ici plus tard
    console.log('Filtre appliqué:', this.selectedFilter);
  }

  addClient(): void {
    this.currentClient = null;
    this.isModalOpen = true;
  }

  editClient(id: number): void {
    this.currentClient = this.clients.find(c => c.id === id) || null;
    this.isModalOpen = true;
  }

  deleteClient(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.loadClients(); // Recharger la liste après suppression
        },
        error: (err: any) => console.error('Failed to delete client', err)
      });
    }
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.currentClient = null;
  }

  saveClient(clientData: any): void {
    if (clientData.id) {
      // Logique de modification
      this.userService.updateUser(clientData.id, clientData).subscribe({
        next: () => {
          this.loadClients(); // Recharger la liste après modification
          this.closeModal();
        },
        error: (err: any) => console.error('Failed to update client', err)
      });
    } else {
      // Logique d'ajout
      this.userService.createUser(clientData).subscribe({
        next: () => {
          this.loadClients(); // Recharger la liste après ajout
          this.closeModal();
        },
        error: (err: any) => console.error('Failed to create client', err)
      });
    }
  }
}