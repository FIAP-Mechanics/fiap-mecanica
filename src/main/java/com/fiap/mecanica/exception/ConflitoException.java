package com.fiap.mecanica.exception;

public abstract class ConflitoException extends BaseException {
    protected ConflitoException(String mensagem) {
        super(mensagem);
    }
}