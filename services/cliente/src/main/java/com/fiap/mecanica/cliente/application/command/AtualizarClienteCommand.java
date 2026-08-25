package com.fiap.mecanica.cliente.application.command;

import com.fiap.mecanica.cliente.domain.Endereco;
import lombok.Builder;

@Builder
public record AtualizarClienteCommand(
        String nome,
        String documento,
        String email,
        String telefone,
        Endereco endereco
) {
}
