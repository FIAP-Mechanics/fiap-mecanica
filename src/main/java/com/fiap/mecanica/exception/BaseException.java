package com.fiap.mecanica.exception;

public abstract class BaseException extends RuntimeException {
    protected BaseException(String mensagem) {
        super(mensagem);
    }
}