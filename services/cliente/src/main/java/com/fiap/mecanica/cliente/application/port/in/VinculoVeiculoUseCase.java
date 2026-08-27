package com.fiap.mecanica.cliente.application.port.in;

import com.fiap.mecanica.cliente.domain.ClienteVeiculo;

import java.util.List;

public interface VinculoVeiculoUseCase {

    List<ClienteVeiculo> listarVeiculosDoCliente(Long clienteId);

    void vincularVeiculo(Long clienteId, Long veiculoId, String placa, String marca, String modelo, Integer ano);
}
