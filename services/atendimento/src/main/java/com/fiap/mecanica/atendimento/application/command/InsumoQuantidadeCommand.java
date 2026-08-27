package com.fiap.mecanica.atendimento.application.command;

public record InsumoQuantidadeCommand(
        Long insumo,
        Integer quantidade
) {
}
