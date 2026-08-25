package com.fiap.mecanica.atendimento.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteIntegracaoDto(
        Long id,
        String nome,
        String documento,
        String email
) {
}
