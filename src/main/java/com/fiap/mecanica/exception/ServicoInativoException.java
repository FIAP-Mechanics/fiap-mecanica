package com.fiap.mecanica.exception;

public class ServicoInativoException extends RuntimeException {
    public ServicoInativoException(Long id) {
        super("O Servico com ID " + id + " encontra-se inativo");
    }
}
