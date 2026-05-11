import { Component } from '@angular/core';
import { RouterModule } from '@angular/router'; // Para que reconozca <router-outlet>
import { Sidebar } from '../../shared/components/sidebar/sidebar'; // Ajusta la ruta si es necesario
import { NavbarAdmin } from '../../shared/components/navbar-admin/navbar-admin'; // Ajusta la ruta si es necesario

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  // ¡AQUÍ ESTÁ LA MAGIA! Le decimos a Angular qué componentes usará este Layout
  imports: [RouterModule, Sidebar, NavbarAdmin], 
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css'
})
export class AdminLayout {

}