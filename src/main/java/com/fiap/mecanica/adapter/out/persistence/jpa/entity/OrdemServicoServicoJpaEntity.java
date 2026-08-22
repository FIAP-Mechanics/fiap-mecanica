package com.fiap.mecanica.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "OrdemServicoServico")
@Table(name = "ordem_servico_servico")
public class OrdemServicoServicoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orcamento_id")
    @ToString.Exclude
    private OrcamentoJpaEntity orcamento;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private ServicoJpaEntity servico;

    private Integer quantidade;

    private Long tempoExecucaoMinutos;
}
