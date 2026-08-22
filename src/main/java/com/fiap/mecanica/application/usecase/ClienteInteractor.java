package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.application.port.in.ClienteUseCase;
import com.fiap.mecanica.application.port.out.ClienteGateway;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.exception.ClienteExistente;
import com.fiap.mecanica.exception.ClienteNotFound;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ClienteInteractor implements ClienteUseCase {

    private final ClienteGateway clienteGateway;

    public ClienteInteractor(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    @Override
    public List<Cliente> buscarClientes() {
        return clienteGateway.buscarTodos();
    }

    @Override
    public Cliente buscarClientePorId(Long id) {
        return clienteGateway.buscarPorId(id).orElseThrow(() -> new ClienteNotFound(id));
    }

    @Override
    public Cliente buscarClientePorDocumento(String documento) {
        String documentoNormalizado = apenasDigitos(documento);
        return clienteGateway.buscarPorDocumento(documentoNormalizado)
                .orElseThrow(() -> new ClienteNotFound(documentoNormalizado));
    }

    @Override
    public Cliente cadastrarCliente(Cliente cliente) {
        if (clienteGateway.existePorDocumento(cliente.getDocumento())) {
            throw new ClienteExistente(cliente.getDocumento());
        }
        return clienteGateway.salvar(cliente);
    }

    @Override
    public Cliente atualizarCliente(Long id, AtualizarClienteCommand command) {
        Cliente cliente = clienteGateway.buscarPorId(id).orElseThrow(() -> new ClienteNotFound(id));
        atualizarCampo(command.nome(), cliente.getNome(), cliente::setNome);
        atualizarCampo(command.documento(), cliente.getDocumento(), cliente::setDocumento);
        atualizarCampo(command.email(), cliente.getEmail(), cliente::setEmail);
        atualizarCampo(command.telefone(), cliente.getTelefone(), cliente::setTelefone);
        atualizarCampo(command.endereco(), cliente.getEndereco(), cliente::setEndereco);
        return clienteGateway.salvar(cliente);
    }

    private <T> void atualizarCampo(T valorNovo, T valorAntigo, Consumer<T> setter) {
        if (valorNovo != null && !Objects.equals(valorNovo, valorAntigo)) {
            setter.accept(valorNovo);
        }
    }

    private String apenasDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
