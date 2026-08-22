package com.fiap.mecanica.cliente.exception;

public abstract class EntidadeNaoEncontradaException extends BaseException {
    protected EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
