import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type StepState = 'completado' | 'activo' | 'pendiente';

export type CheckoutStep = 'seats' | 'tickets' | 'snacks' | 'payment';

interface StepConfig {
  id: CheckoutStep;
  label: string;
  icon: string;
}

const STEPS: StepConfig[] = [
  { id: 'seats',   label: 'Asientos', icon: 'fa-solid fa-couch'          },
  { id: 'tickets', label: 'Tickets',  icon: 'fa-solid fa-ticket'         },
  { id: 'snacks',  label: 'Snacks',   icon: 'fa-solid fa-burger'         },
  { id: 'payment', label: 'Pago',     icon: 'fa-regular fa-credit-card'  },
];

@Component({
  selector: 'app-checkout-stepper',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout-stepper.html',
  styleUrl: './checkout-stepper.css'
})
export class CheckoutStepperComponent {

  @Input() pasoActual: CheckoutStep = 'seats';

  readonly steps = STEPS;

  estadoPaso(step: CheckoutStep): StepState {
    const idx        = STEPS.findIndex(s => s.id === step);
    const idxActual  = STEPS.findIndex(s => s.id === this.pasoActual);

    if (idx < idxActual)  return 'completado';
    if (idx === idxActual) return 'activo';
    return 'pendiente';
  }
}