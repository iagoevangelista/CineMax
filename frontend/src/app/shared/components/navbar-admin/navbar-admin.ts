import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ROLE_LABELS } from '../../../pages/utils/role-labels'; 
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-navbar-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar-admin.html',
  styleUrl: './navbar-admin.css'
})
export class NavbarAdmin implements OnInit {
  
  // Exponemos la función al HTML
  public ROLE_LABELS = ROLE_LABELS;
  rolActual: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.rolActual = this.authService.getRole() || '';
  }
}