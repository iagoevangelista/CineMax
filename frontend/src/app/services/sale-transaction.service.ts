import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// ── DTOs que reflejan exactamente el backend ──────────────────────────────────

export interface TicketLineDTO {
  /** "ADULTO" | "NINO" | "ADULTO_MAYOR" | "DISCAPACITADO" */
  categoryCode: string;
  cantidad: number;
  precioUnitario: number;
}

export interface SnackLineDTO {
  idSnack: number;
  cantidad: number;
  unitPrice: number;
}

export interface SaleTransactionRequestDTO {
  idShowtime: number;
  asientosIds: number[];
  tickets: TicketLineDTO[];
  snacks: SnackLineDTO[];
  subtotal: number;
  discountAmount: number;
  totalAmount: number;
  /** "TARJETA" | "YAPE" */
  paymentMethod: 'TARJETA' | 'YAPE';
  /** null si no se aplicó promo */
  idPromotion: number | null;
}

export interface SaleTransactionResponseDTO {
  idTransaction: number;
  qrCodeData: string;
  message: string;
}

// ─────────────────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class SaleTransactionService {

  private readonly apiUrl = 'http://localhost:8080/api/v1/sale-transactions';

  constructor(private http: HttpClient) {}

  /**
   * POST /api/v1/sale-transactions
   *
   * El interceptor (auth.interceptor.ts) adjunta automáticamente el JWT,
   * así el backend puede extraer el email del usuario desde Authentication.
   *
   * Devuelve { idTransaction, qrCodeData, message } si todo sale bien,
   * o lanza un error HTTP con el mensaje en err.error (string plano del backend).
   */
  createSaleTransaction(request: SaleTransactionRequestDTO): Observable<SaleTransactionResponseDTO> {
    return this.http.post<SaleTransactionResponseDTO>(this.apiUrl, request);
  }
}