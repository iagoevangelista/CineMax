import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar implements OnInit {

  permisos: string[] = [];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.permisos = this.authService.getPermissions();
  }

  tiene(permiso: string): boolean {
    return this.permisos.includes(permiso);
  }

  cerrarSesion() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
