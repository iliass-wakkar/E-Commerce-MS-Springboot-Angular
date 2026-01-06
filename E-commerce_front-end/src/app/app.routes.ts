import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { DashboardComponent } from './pages/admin/dashboard/dashboard.component';
import { OverviewComponent } from './pages/admin/dashboard/overview.component';
import { ClientsComponent } from './pages/admin/dashboard/clients.component';
import { ProductsComponent } from './pages/admin/dashboard/products.component';
import { OrdersComponent } from './pages/admin/dashboard/orders.component';
import { AdminProfileComponent } from './pages/admin/dashboard/profile.component';
import { OrdersComponent as ShopOrdersComponent } from './components/orders/orders.component';
import { ProductDetailComponent } from './pages/product-detail/product-detail.component';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'admin/dashboard',
    component: DashboardComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      { path: 'overview', component: OverviewComponent },
      { path: 'clients', component: ClientsComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'profile', component: AdminProfileComponent }
    ]
  },
  {
    path: 'orders',
    component: ShopOrdersComponent,
    canActivate: [authGuard]
  },
  {
    path: 'products/:id',
    component: ProductDetailComponent
  }
];
