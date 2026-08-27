package com.fiap.mecanica.cliente.application.port.in;

import com.fiap.mecanica.cliente.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.cliente.domain.Cliente;

import java.util.List;

public interface ClienteUseCase {

    List<Cliente> buscarClientes();

    Cliente buscarClientePorId(Long id);

    Cliente buscarClientePorDocumento(String documento);

    Cliente cadastrarCliente(Cliente cliente);

    Cliente atualizarCliente(Long id, AtualizarClienteCommand command);
}
