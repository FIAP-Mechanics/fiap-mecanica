package com.fiap.mecanica.exception;

public class EstoqueJaAtivoException extends RuntimeException {
    public EstoqueJaAtivoException(Long idInsumo) {
        super("O registro de estoque do insum com ID " + idInsumo + " ja se encontra ativo.");
    }
}
