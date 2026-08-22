package com.fiap.mecanica.funcionario.exception;

public abstract class BaseException extends RuntimeException {
    protected BaseException(String mensagem) {
        super(mensagem);
    }
}
