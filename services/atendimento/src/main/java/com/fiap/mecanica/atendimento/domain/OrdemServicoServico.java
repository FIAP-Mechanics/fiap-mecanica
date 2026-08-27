package com.fiap.mecanica.atendimento.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoServico {

    private Long id;

    @ToString.Exclude
    private Orcamento orcamento;

    private Long servicoId;

    private String nomeServico;

    private BigDecimal valorUnitario;

    private Integer quantidade;
}
