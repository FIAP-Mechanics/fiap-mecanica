package com.fiap.mecanica.estoque.application.command;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AtualizarInsumoCommand(
        String nome,
        BigDecimal precoUnitario
) {
}
