package com.fiap.mecanica.exception;

public class VeiculoNaoEncontradoException extends EntidadeNaoEncontradaException {

    public VeiculoNaoEncontradoException(Long id) {
        super("Veículo não encontrado com ID: " + id);
    }
}