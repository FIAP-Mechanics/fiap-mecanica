package com.fiap.mecanica.exception;

public class VeiculoInativoException extends ValidacaoException {

    public VeiculoInativoException(Long id) {
        super("Veículo inativo com ID: " + id);
    }
}