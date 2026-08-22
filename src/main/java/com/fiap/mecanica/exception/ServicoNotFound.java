package com.fiap.mecanica.exception;

public class ServicoNotFound extends RuntimeException {
    public ServicoNotFound(Long id) {
        super("Servico nao encontrado com ID: " + id);
    }
}
