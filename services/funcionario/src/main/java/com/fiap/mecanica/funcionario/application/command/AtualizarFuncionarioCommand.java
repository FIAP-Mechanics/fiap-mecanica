package com.fiap.mecanica.funcionario.application.command;

import com.fiap.mecanica.funcionario.domain.Funcao;
import lombok.Builder;

@Builder
public record AtualizarFuncionarioCommand(
        String email,
        String nome,
        String senha,
        Funcao funcao
) {
}
