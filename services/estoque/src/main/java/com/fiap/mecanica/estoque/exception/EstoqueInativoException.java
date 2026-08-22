package com.fiap.mecanica.estoque.exception;

public class EstoqueInativoException extends ValidacaoException {
    public EstoqueInativoException(Long idInsumo) {
        super("O registro de estoque do insumo com ID " + idInsumo + " encontra-se inativo");
    }
}
