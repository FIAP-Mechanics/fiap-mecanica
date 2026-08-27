package com.fiap.mecanica.estoque.exception;

public class EstoqueNotFound extends EntidadeNaoEncontradaException {
    public EstoqueNotFound(Long idInsumo) {
        super("Registro de estoque não encontrado para o insumo com ID: " + idInsumo);
    }
}
