package com.fiap.mecanica.servico.exception;

public class ServicoNotFound extends EntidadeNaoEncontradaException {
    public ServicoNotFound(Long id) {
        super("Serviço não encontrado com ID: " + id);
    }
}
