package com.fiap.mecanica.atendimento.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "servico_id")
    private Long servicoId;

    @Column(name = "nome_servico")
    private String nomeServico;

    @Column(name = "valor_unitario", precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    private Integer quantidade;
}
