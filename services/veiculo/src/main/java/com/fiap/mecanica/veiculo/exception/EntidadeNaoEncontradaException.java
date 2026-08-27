package com.fiap.mecanica.veiculo.exception;

public abstract class EntidadeNaoEncontradaException extends BaseException {
    protected EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
