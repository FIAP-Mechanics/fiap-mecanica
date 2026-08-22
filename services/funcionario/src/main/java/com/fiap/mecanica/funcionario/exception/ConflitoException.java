package com.fiap.mecanica.funcionario.exception;

public abstract class ConflitoException extends BaseException {
    protected ConflitoException(String mensagem) {
        super(mensagem);
    }
}
