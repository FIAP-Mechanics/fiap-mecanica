package com.fiap.mecanica.cliente.exception;

public class ClienteNotFound extends EntidadeNaoEncontradaException {
    public ClienteNotFound(Long id) {
        super("Cliente não encontrado. ID: " + id);
    }

    public ClienteNotFound(String documento) {
        super("Cliente não encontrado. Documento: " + documento);
    }
}
