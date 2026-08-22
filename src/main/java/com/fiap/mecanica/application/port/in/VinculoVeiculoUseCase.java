package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.domain.Veiculo;

import java.util.List;

public interface VinculoVeiculoUseCase {
    List<Veiculo> listarVeiculosDoCliente(Long clienteId);
    void vincularVeiculo(Long clienteId, Long veiculoId);
}
