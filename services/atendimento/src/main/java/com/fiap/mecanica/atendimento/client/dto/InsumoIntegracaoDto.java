package com.fiap.mecanica.atendimento.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InsumoIntegracaoDto(
        Long id,
        String nome,
        BigDecimal precoUnitario
) {
}
