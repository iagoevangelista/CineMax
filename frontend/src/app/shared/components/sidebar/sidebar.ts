import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule], // <- Vital para que funcione el routerLink
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar { }