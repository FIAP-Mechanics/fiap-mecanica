package com.fiap.mecanica.exception;

public class ServicoJaAtivoException extends RuntimeException {
    public ServicoJaAtivoException(Long id) {
        super("O Servico com ID " + id + " ja se encontra ativo.");
    }
}
