package com.fiap.mecanica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ServicoInativoException extends RuntimeException {
    public ServicoInativoException(Long id) {
        super("O Servico com ID " + id + " encontra-se inativo");
    }
}
