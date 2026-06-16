package com.fiap.mecanica.exception;

public class ClienteNotFound extends RuntimeException {
    public ClienteNotFound(Long id) {
        super("Cliente não encontrado. ID: " + id);
    }
}
