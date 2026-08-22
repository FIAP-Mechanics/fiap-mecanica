package com.fiap.mecanica.atendimento.exception;

public class ServicoNaoEncontradoException extends EntidadeNaoEncontradaException {
    public ServicoNaoEncontradoException(Long id) {
        super("Serviço não encontrado com ID: " + id);
    }
}
