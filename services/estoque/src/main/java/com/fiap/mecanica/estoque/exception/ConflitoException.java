package com.fiap.mecanica.estoque.exception;

public abstract class ConflitoException extends BaseException {
    protected ConflitoException(String mensagem) {
        super(mensagem);
    }
}
