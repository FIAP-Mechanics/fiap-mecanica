package com.fiap.mecanica.veiculo.exception;

public class VeiculoNaoEncontradoException extends EntidadeNaoEncontradaException {
    public VeiculoNaoEncontradoException(Long id) {
        super("Veículo não encontrado com ID: " + id);
    }
}
