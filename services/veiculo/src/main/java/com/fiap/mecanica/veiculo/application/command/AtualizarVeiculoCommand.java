package com.fiap.mecanica.veiculo.application.command;

import lombok.Builder;

@Builder
public record AtualizarVeiculoCommand(
        String marca,
        String modelo,
        String placa,
        Integer ano
) {
}
