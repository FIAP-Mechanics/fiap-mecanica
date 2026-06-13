package com.fiap.mecanica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ServicoJaAtivoException extends RuntimeException {
    public ServicoJaAtivoException(Long id) {
        super("O Servico com ID " + id + " ja se encontra ativo.");
    }
}
