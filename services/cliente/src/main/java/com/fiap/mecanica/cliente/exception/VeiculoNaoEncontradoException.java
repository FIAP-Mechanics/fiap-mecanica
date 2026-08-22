package com.fiap.mecanica.cliente.exception;

public class VeiculoNaoEncontradoException extends EntidadeNaoEncontradaException {
    public VeiculoNaoEncontradoException(Long id) {
        super("Veículo não encontrado. ID: " + id);
    }
}
