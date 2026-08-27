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
public class OrdemServicoInsumo {

    private Long id;

    @ToString.Exclude
    private Orcamento orcamento;

    private Long insumoId;

    private String nomeInsumo;

    private BigDecimal precoUnitario;

    private Integer quantidade;
}
