package com.fiap.mecanica.estoque.exception;

public class EstoqueJaAtivoException extends ValidacaoException {
    public EstoqueJaAtivoException(Long idInsumo) {
        super("O registro de estoque do insumo com ID " + idInsumo + " já se encontra ativo.");
    }
}
