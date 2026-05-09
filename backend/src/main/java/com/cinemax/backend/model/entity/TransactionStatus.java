package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_status")
public class TransactionStatus {
    @Id
    @Column(name = "id_transaction_status")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTransactionStatus;

    @Column(name = "name_status", nullable = false, unique = true, length = 30)
    private String nameStatus;
}