package com.fiap.mecanica.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Servico")
@Table(name = "servico")
public class ServicoJpaEntity {
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
