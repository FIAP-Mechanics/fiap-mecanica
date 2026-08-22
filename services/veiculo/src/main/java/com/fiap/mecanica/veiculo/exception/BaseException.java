package com.fiap.mecanica.veiculo.exception;

public abstract class BaseException extends RuntimeException {
    protected BaseException(String mensagem) {
        super(mensagem);
    }
}
