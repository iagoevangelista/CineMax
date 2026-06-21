import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Sidebar } from '../../shared/components/sidebar/sidebar'; 
import { NavbarAdmin } from '../../shared/components/navbar-admin/navbar-admin';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterModule, Sidebar, NavbarAdmin], 
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css'
})
export class AdminLayout {

}