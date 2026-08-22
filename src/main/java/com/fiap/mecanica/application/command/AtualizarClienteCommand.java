package com.fiap.mecanica.application.command;

import com.fiap.mecanica.domain.Endereco;

public record AtualizarClienteCommand(
        String nome,
        String documento,
        String email,
        String telefone,
        Endereco endereco) {
}
