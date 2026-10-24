package com.fiap.mecanica.cliente.application.usecase;

import com.fiap.mecanica.cliente.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.cliente.application.port.in.ClienteUseCase;
import com.fiap.mecanica.cliente.application.port.out.ClienteGateway;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.exception.ClienteExistente;
import com.fiap.mecanica.cliente.exception.ClienteInativoException;
import com.fiap.mecanica.cliente.exception.ClienteJaAtivoException;
import com.fiap.mecanica.cliente.exception.ClienteNotFound;

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
        return clienteGateway.buscarTodos().stream()
                .filter(Cliente::isAtivo)
                .toList();
    }

    @Override
    public Cliente buscarClientePorId(Long id) {
        Cliente cliente = clienteGateway.buscarPorId(id).orElseThrow(() -> new ClienteNotFound(id));
        if (!cliente.isAtivo()) throw new ClienteInativoException(id);
        return cliente;
    }

    @Override
    public Cliente buscarClientePorDocumento(String documento) {
        String documentoNormalizado = apenasDigitos(documento);
        Cliente cliente = clienteGateway.buscarPorDocumento(documentoNormalizado)
                .orElseThrow(() -> new ClienteNotFound(documentoNormalizado));
        if (!cliente.isAtivo()) throw new ClienteInativoException(cliente.getId());
        return cliente;
    }

    @Override
    public Cliente cadastrarCliente(Cliente cliente) {
        String documentoNormalizado = apenasDigitos(cliente.getDocumento());
        cliente.setDocumento(documentoNormalizado);
        boolean clienteEncontrado = clienteGateway.existePorDocumento(documentoNormalizado);
        if (clienteEncontrado) {
            throw new ClienteExistente(cliente.getDocumento());
        }
        return clienteGateway.salvar(cliente);
    }

    @Override
    public Cliente atualizarCliente(Long id, AtualizarClienteCommand command) {
        Cliente cliente = buscarClientePorId(id);
        atualizarCampo(command.nome(), cliente.getNome(), cliente::setNome);
        atualizarCampo(command.documento() != null ? apenasDigitos(command.documento()) : null, cliente.getDocumento(), cliente::setDocumento);
        atualizarCampo(command.email(), cliente.getEmail(), cliente::setEmail);
        atualizarCampo(command.telefone(), cliente.getTelefone(), cliente::setTelefone);
        atualizarCampo(command.endereco(), cliente.getEndereco(), cliente::setEndereco);

        return clienteGateway.salvar(cliente);
    }

    @Override
    public Cliente excluirCliente(Long id) {
        Cliente cliente = buscarClientePorId(id);
        cliente.setAtivo(false);
        return clienteGateway.salvar(cliente);
    }

    @Override
    public Cliente reativarCliente(Long id) {
        Cliente cliente = clienteGateway.buscarPorId(id).orElseThrow(() -> new ClienteNotFound(id));
        if (cliente.isAtivo()) throw new ClienteJaAtivoException(id);
        cliente.setAtivo(true);
        return clienteGateway.salvar(cliente);
    }

    private <T> void atualizarCampo(T valorNovo, T valorAntigo, Consumer<T> setter) {
        if (valorNovo != null && !Objects.equals(valorNovo, valorAntigo)) {
            setter.accept(valorNovo);
        }
    }

    private String apenasDigitos(String valor) {
        return valor != null ? valor.replaceAll("\\D", "") : "";
    }
}
