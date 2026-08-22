package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.application.port.out.ClienteGateway;
import com.fiap.mecanica.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.application.port.out.VeiculoGateway;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.ClienteVeiculo;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.exception.ClienteNotFound;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.exception.VinculoJaExistente;

import java.util.List;

public class VinculoVeiculoInteractor implements VinculoVeiculoUseCase {

    private final ClienteGateway clienteGateway;
    private final VeiculoGateway veiculoGateway;
    private final ClienteVeiculoGateway clienteVeiculoGateway;

    public VinculoVeiculoInteractor(
            ClienteGateway clienteGateway,
            VeiculoGateway veiculoGateway,
            ClienteVeiculoGateway clienteVeiculoGateway) {
        this.clienteGateway = clienteGateway;
        this.veiculoGateway = veiculoGateway;
        this.clienteVeiculoGateway = clienteVeiculoGateway;
    }

    @Override
    public List<Veiculo> listarVeiculosDoCliente(Long clienteId) {
        clienteGateway.buscarPorId(clienteId).orElseThrow(() -> new ClienteNotFound(clienteId));
        return clienteVeiculoGateway.buscarPorClienteId(clienteId).stream()
                .map(ClienteVeiculo::getVeiculo)
                .toList();
    }

    @Override
    public void vincularVeiculo(Long clienteId, Long veiculoId) {
        Cliente cliente = clienteGateway.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNotFound(clienteId));
        Veiculo veiculo = veiculoGateway.buscarPorId(veiculoId)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(veiculoId));

        if (clienteVeiculoGateway.existeVinculo(clienteId, veiculoId)) {
            throw new VinculoJaExistente(clienteId, veiculoId);
        }

        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .build();
        clienteVeiculoGateway.salvar(vinculo);
    }
}
