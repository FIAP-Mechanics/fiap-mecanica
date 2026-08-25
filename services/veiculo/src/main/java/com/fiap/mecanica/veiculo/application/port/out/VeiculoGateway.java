package com.fiap.mecanica.veiculo.application.port.out;

import com.fiap.mecanica.veiculo.domain.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoGateway {

    List<Veiculo> buscarTodos();

    Optional<Veiculo> buscarPorId(Long id);

    Optional<Veiculo> buscarPorPlaca(String placa);

    boolean existsByPlaca(String placa);

    Veiculo salvar(Veiculo veiculo);
}
