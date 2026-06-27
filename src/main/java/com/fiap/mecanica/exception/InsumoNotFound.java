package com.fiap.mecanica.exception;

public class InsumoNotFound extends EntidadeNaoEncontradaException {

    public InsumoNotFound(Long id) {
        super("Insumo não encontrado com ID: " + id);
    }
}
