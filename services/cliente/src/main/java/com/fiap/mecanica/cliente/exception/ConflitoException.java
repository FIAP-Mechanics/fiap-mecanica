package com.fiap.mecanica.cliente.exception;

public abstract class ConflitoException extends BaseException {
    protected ConflitoException(String mensagem) {
        super(mensagem);
    }
}
