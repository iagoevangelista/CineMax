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
import { ResetPasswordComponent } from './pages/reset-password/reset-password';
import { Profile } from './pages/profile/profile';

export const routes: Routes = [
  {
    path: '',
    component: ClientLayout,
    children: [
      { path: '', component: Home },
      { path: 'movies', component: Movies },
      { 
        path: 'movie/:id', 
        component: MovieDetail,
        runGuardsAndResolvers: 'always'
      },
      { path: 'seats', component: Seats },
      { path: 'tickets', component: Tickets },
      { path: 'snacks', component: Snacks },
      { path: 'reset-password', component: ResetPasswordComponent }
    ]
  },
  {
    path: 'admin',
    component: AdminLayout,
    children: [
      { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
      { path: 'sedes', component: Venues, canActivate: [authGuard], data: { expectedRole: 'GERENTE_GRAL' } },
      { path: 'usuarios', component: Users, canActivate: [authGuard], data: { expectedRole: 'ADMIN' } },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  { path: '', component: Home },
  { path: 'perfil', component: Profile },
  { path: '**', redirectTo: '' }
];