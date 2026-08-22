package com.fiap.mecanica.atendimento.client.dto;

public record DeduzirEstoqueItemDto(
        Long insumoId,
        Integer quantidade
) {
}
