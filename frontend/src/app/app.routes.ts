import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { AdminLayout } from './layouts/admin-layout/admin-layout'; // Nuevo
import { Venues } from './pages/admin/venues/venues';
import { Users } from './pages/admin/users/users';
import { Dashboard } from './pages/admin/dashboard/dashboard';

export const routes: Routes = [
  { path: '', component: Home }, // Página pública
  
  {
    path: 'admin',
    component: AdminLayout, // El esqueleto con Sidebar y Navbar
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'sedes', component: Venues },
      { path: 'usuarios', component: Users },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' } 
    ]
  },

  { path: '**', redirectTo: '' }
];