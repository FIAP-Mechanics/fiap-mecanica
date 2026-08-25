package com.fiap.mecanica.servico.application.command;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AtualizarServicoCommand(
        String nome,
        String descricao,
        BigDecimal valor
) {
}
