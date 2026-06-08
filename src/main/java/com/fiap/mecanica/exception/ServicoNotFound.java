package com.fiap.mecanica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ServicoNotFound extends RuntimeException {
    public ServicoNotFound(Long id) {
        super("Servico nao encontrado com ID: " + id);
    }
}
