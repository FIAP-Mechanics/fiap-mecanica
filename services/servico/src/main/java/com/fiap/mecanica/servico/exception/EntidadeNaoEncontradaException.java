package com.fiap.mecanica.servico.exception;

public abstract class EntidadeNaoEncontradaException extends BaseException {
    protected EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
