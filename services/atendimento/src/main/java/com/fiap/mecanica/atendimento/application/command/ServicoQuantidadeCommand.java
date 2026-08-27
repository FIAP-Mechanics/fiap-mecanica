package com.fiap.mecanica.atendimento.application.command;

public record ServicoQuantidadeCommand(
        Long servico,
        Integer quantidade
) {
}
