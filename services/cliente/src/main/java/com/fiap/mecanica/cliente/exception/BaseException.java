package com.fiap.mecanica.cliente.exception;

public abstract class BaseException extends RuntimeException {
    protected BaseException(String mensagem) {
        super(mensagem);
    }
}
