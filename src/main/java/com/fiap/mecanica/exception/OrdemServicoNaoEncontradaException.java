package com.fiap.mecanica.exception;

public class OrdemServicoNaoEncontradaException extends EntidadeNaoEncontradaException {

    public OrdemServicoNaoEncontradaException(String id) {
        super("Ordem de serviço não encontrada com ID: " + id);
    }
}
