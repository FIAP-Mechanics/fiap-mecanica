package com.fiap.mecanica.atendimento.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordem_servico_insumo")
public class OrdemServicoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orcamento_id")
    @ToString.Exclude
    private Orcamento orcamento;

    @Column(name = "insumo_id")
    private Long insumoId;

    @Column(name = "nome_insumo")
    private String nomeInsumo;

    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    private Integer quantidade;
}
