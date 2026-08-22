package com.fiap.mecanica.servico.exception;

public abstract class BaseException extends RuntimeException {
    protected BaseException(String mensagem) {
        super(mensagem);
    }
}
