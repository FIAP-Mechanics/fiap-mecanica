package com.fiap.mecanica.veiculo.application.port.in;

import com.fiap.mecanica.veiculo.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.veiculo.domain.Veiculo;

import java.util.List;

public interface VeiculoUseCase {

    List<Veiculo> buscarTodos();

    Veiculo cadastrarVeiculo(Veiculo veiculo);

    Veiculo buscarVeiculoPorId(Long id);

    Veiculo buscarPorPlaca(String placa);

    Veiculo atualizarVeiculo(Long id, AtualizarVeiculoCommand command);

    Veiculo excluirVeiculo(Long id);

    Veiculo reativarVeiculo(Long id);
}
