package com.fiap.mecanica.funcionario.exception;

public abstract class EntidadeNaoEncontradaException extends BaseException {
    protected EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
