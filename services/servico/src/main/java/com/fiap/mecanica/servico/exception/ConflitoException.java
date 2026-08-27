package com.fiap.mecanica.servico.exception;

public abstract class ConflitoException extends BaseException {
    protected ConflitoException(String mensagem) {
        super(mensagem);
    }
}
