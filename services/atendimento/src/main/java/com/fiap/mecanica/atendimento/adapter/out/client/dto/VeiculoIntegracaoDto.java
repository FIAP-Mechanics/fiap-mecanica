package com.fiap.mecanica.atendimento.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VeiculoIntegracaoDto(
        Long id,
        String placa
) {
}
