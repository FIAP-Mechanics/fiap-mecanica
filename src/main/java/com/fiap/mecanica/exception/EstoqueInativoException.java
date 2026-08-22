package com.fiap.mecanica.exception;

public class EstoqueInativoException extends RuntimeException {
    public EstoqueInativoException(Long idInsumo) {
        super("O registro de estoque do insum com ID " + idInsumo + " encontra-se inativo");
    }
}
