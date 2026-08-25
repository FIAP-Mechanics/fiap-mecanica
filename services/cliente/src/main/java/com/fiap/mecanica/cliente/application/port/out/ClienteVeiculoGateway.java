package com.fiap.mecanica.cliente.application.port.out;

import com.fiap.mecanica.cliente.domain.ClienteVeiculo;

import java.util.List;

public interface ClienteVeiculoGateway {

    boolean existeVinculo(Long clienteId, Long veiculoId);

    List<ClienteVeiculo> buscarPorClienteId(Long clienteId);

    ClienteVeiculo salvar(ClienteVeiculo vinculo);
}
