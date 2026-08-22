package com.fiap.mecanica.servico.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String nome;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;
    @Builder.Default
    private boolean ativo = true;
}
