package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.domain.Veiculo;

public interface VeiculoUseCase {
    Veiculo cadastrarVeiculo(Veiculo veiculo);
    Veiculo buscarVeiculoPorId(Long id);
    Veiculo atualizarVeiculo(Long id, AtualizarVeiculoCommand command);
    Veiculo excluirVeiculo(Long id);
    Veiculo reativarVeiculo(Long id);
}
