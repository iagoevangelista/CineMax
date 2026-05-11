import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';


@Component({
  selector: 'app-navbar-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar-admin.html',
  styleUrl: './navbar-admin.css'
})
export class NavbarAdmin implements OnInit {
  
  rolActual: string = 'Cargando...'; 

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Le pedimos al servicio que lea el token y nos diga el rol
    this.rolActual = this.authService.getRole();
  }
}