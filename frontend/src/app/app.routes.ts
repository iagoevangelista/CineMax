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
import { Tickets } from './pages/checkout/tickets/tickets';
import { ClientLayout } from './layouts/client-layout/client-layout';
import { Snacks } from './pages/checkout/snacks/snacks';
import { Payment } from './pages/checkout/payment/payment';
import { ResetPasswordComponent } from './pages/reset-password/reset-password';
import { Profile } from './pages/profile/profile';
import { Sedes } from './pages/sedes/sedes';
import { Rooms } from './pages/admin/rooms/rooms';
import { AdminMovies } from './pages/admin/movies/movies';
import { AdminShowtimes } from './pages/admin/showtimes/showtimes';
import { Confiteria } from './pages/admin/confiteria/confiteria';
import { Confiteria as ConfiteriaPublica } from './pages/confiteria/confiteria';
export const routes: Routes = [
  {
    path: '',
    component: ClientLayout,
    children: [
      { path: '', component: Home },
      { path: 'profile', component: Profile },
      { path: 'movies', component: Movies },
      { path: 'sedes', component: Sedes },
      { path: 'confiteria', component: ConfiteriaPublica },
      { 
        path: 'movie/:id', 
        component: MovieDetail,
        runGuardsAndResolvers: 'always'
      },
      { path: 'seats', component: Seats },
      { path: 'tickets', component: Tickets },
      { path: 'snacks', component: Snacks },
      { path: 'payment', component: Payment },
      { path: 'reset-password', component: ResetPasswordComponent }
      
    ]
  },
    {
    path: 'admin',
    component: AdminLayout,
    children: [
      { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
      
      // Sedes: Solo Admin y Gerente General
      { path: 'sedes', component: Venues, canActivate: [authGuard], data: { expectedRoles: ['GERENTE_GENERAL'] } },
      
      // Salas: Admin, G. General y G. de Operaciones
      { path: 'salas', component: Rooms, canActivate: [authGuard], data: { expectedRoles: ['GERENTE_GENERAL', 'GERENTE_DE_OPERACIONES'] } },

      // Movies: G. de Operaciones
      { path: 'peliculas', component: AdminMovies, canActivate: [authGuard], data: { expectedRoles: ['GERENTE_GENERAL', 'GERENTE_DE_OPERACIONES'] } },
      
      { path: 'funciones', component: AdminShowtimes, canActivate: [authGuard], data: { expectedRoles: ['GERENTE_GENERAL', 'GERENTE_DE_OPERACIONES'] } },

      { path: 'confiteria', component: Confiteria, canActivate: [authGuard], data: { expectedRoles: ['GERENTE_GENERAL', 'GERENTE_DE_MARKETING'] } },

      { path: 'usuarios', component: Users, canActivate: [authGuard], data: { expectedRoles: ['ADMIN'] } },
      
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }

    ]
  },

  { path: '', component: Home },
  { path: 'perfil', component: Profile },
  { path: '**', redirectTo: '' }
];