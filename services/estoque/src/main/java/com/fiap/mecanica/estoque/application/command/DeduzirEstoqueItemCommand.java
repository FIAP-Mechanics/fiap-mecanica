package com.fiap.mecanica.estoque.application.command;

import lombok.Builder;

@Builder
public record DeduzirEstoqueItemCommand(
        Long insumoId,
        Long quantidade
) {
}
