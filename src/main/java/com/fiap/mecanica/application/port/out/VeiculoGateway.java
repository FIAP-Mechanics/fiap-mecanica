package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.Veiculo;

import java.util.Optional;

public interface VeiculoGateway {
    Optional<Veiculo> buscarPorId(Long id);
    boolean existePorPlaca(String placa);
    Veiculo salvar(Veiculo veiculo);
}
