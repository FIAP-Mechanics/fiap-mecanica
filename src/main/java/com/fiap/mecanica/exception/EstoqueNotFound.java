package com.fiap.mecanica.exception;

public class EstoqueNotFound extends RuntimeException {
    public EstoqueNotFound(Long idInsumo) {
        super("Registro de estoque nao encontrado para o insum com ID: " + idInsumo);
    }
}
