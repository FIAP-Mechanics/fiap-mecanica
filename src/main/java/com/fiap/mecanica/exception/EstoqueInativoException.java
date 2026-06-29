package com.fiap.mecanica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EstoqueInativoException extends RuntimeException {
    public EstoqueInativoException(Long idInsumo) {
        super("O registro de estoque do insum com ID " + idInsumo + " encontra-se inativo");
    }
}
