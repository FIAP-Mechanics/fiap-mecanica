package com.fiap.mecanica.estoque.exception;

public abstract class EntidadeNaoEncontradaException extends BaseException {
    protected EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
