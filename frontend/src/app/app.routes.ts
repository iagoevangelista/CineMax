import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { AdminLayout } from './layouts/admin-layout/admin-layout';
import { Venues } from './pages/admin/venues/venues';
import { Users } from './pages/admin/users/users';
import { Dashboard } from './pages/admin/dashboard/dashboard';
import { authGuard } from './services/auth.guard';
import { MovieDetail } from './pages/movie-detail/movie-detail';
import { Movies } from './pages/movies/movies';
import { Seats } from './pages/checkout/seats/seats';
import { ClientLayout } from './layouts/client-layout/client-layout';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'movie/:id', component: MovieDetail },
  { path: 'movies', component: Movies },

  {
    path: '',
    component: ClientLayout, // Este es el padre
    children: [
      { path: 'seats', component: Seats }, // Esta es la hija que aparecerá en el router-outlet
      // { path: 'tickets', component: Tickets },
      // ... más páginas del cliente
    ]
  },
  
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'sedes', component: Venues },
      { path: 'usuarios', component: Users },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: '' }
];