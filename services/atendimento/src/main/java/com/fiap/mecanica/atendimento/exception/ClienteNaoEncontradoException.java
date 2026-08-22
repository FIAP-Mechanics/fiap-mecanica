package com.fiap.mecanica.atendimento.exception;

public class ClienteNaoEncontradoException extends EntidadeNaoEncontradaException {
    public ClienteNaoEncontradoException(Long id) {
        super("Cliente não encontrado com ID: " + id);
    }
}
