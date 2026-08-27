package com.fiap.mecanica.atendimento.exception;

public class InsumoNaoEncontradoException extends EntidadeNaoEncontradaException {
    public InsumoNaoEncontradoException(Long id) {
        super("Insumo não encontrado com ID: " + id);
    }
}
