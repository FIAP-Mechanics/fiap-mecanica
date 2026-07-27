package com.fiap.mecanica.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordem_servico_servico")
public class OrdemServicoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orcamento_id")
    @ToString.Exclude
    private Orcamento orcamento;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    private Integer quantidade;

    private Long tempoExecucaoMinutos;
}
