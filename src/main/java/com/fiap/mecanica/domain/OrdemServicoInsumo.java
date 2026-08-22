package com.fiap.mecanica.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    private Integer quantidade;
}
