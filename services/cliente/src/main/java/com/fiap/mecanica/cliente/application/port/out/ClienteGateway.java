package com.fiap.mecanica.cliente.application.port.out;

import com.fiap.mecanica.cliente.domain.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteGateway {

    List<Cliente> buscarTodos();

    Optional<Cliente> buscarPorId(Long id);

    Optional<Cliente> buscarPorDocumento(String documento);

    boolean existePorDocumento(String documento);

    Cliente salvar(Cliente cliente);
}
