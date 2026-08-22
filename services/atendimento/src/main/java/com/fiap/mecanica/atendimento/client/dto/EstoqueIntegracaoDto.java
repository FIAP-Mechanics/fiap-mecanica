package com.fiap.mecanica.atendimento.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EstoqueIntegracaoDto(
        InsumoIntegracaoDto insumo,
        Long quantidadeInsumo
) {
}
