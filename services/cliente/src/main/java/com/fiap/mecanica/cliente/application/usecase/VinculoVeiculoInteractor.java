package com.fiap.mecanica.cliente.application.usecase;

import com.fiap.mecanica.cliente.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.cliente.application.port.out.ClienteGateway;
import com.fiap.mecanica.cliente.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;
import com.fiap.mecanica.cliente.exception.VinculoJaExistente;

import java.util.List;

public class VinculoVeiculoInteractor implements VinculoVeiculoUseCase {

    private final ClienteGateway clienteGateway;
    private final ClienteVeiculoGateway clienteVeiculoGateway;

    public VinculoVeiculoInteractor(ClienteGateway clienteGateway, ClienteVeiculoGateway clienteVeiculoGateway) {
        this.clienteGateway = clienteGateway;
        this.clienteVeiculoGateway = clienteVeiculoGateway;
    }

    @Override
    public List<ClienteVeiculo> listarVeiculosDoCliente(Long clienteId) {
        clienteGateway.buscarPorId(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));
        return clienteVeiculoGateway.buscarPorClienteId(clienteId);
    }

    @Override
    public void vincularVeiculo(Long clienteId, Long veiculoId, String placa, String marca, String modelo, Integer ano) {
        Cliente cliente = clienteGateway.buscarPorId(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));

        if (clienteVeiculoGateway.existeVinculo(clienteId, veiculoId)) {
            throw new VinculoJaExistente(clienteId, veiculoId);
        }

        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .cliente(cliente)
                .veiculoId(veiculoId)
                .placa(placa)
                .marca(marca)
                .modelo(modelo)
                .ano(ano)
                .build();

        clienteVeiculoGateway.salvar(vinculo);
    }
}
