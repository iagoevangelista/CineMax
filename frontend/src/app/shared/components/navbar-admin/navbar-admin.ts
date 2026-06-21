import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-navbar-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar-admin.html',
  styleUrl: './navbar-admin.css'
})
export class NavbarAdmin implements OnInit {

  rolActual: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.rolActual = this.authService.getRole() || '';
  }
  
  formatearRol(roleName: string): string {
    if (!roleName) return '';
    return roleName
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, letra => letra.toUpperCase());
  }
}