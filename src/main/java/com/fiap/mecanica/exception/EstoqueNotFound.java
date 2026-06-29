package com.fiap.mecanica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EstoqueNotFound extends RuntimeException {
    public EstoqueNotFound(Long idInsumo) {
        super("Registro de estoque nao encontrado para o insum com ID: " + idInsumo);
    }
}
