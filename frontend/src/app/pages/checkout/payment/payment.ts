import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './payment.html',
  styleUrl: './payment.css',
})
export class Payment {}