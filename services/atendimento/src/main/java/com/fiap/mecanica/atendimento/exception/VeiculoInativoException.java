package com.fiap.mecanica.atendimento.exception;

public class VeiculoInativoException extends ConflitoException {
    public VeiculoInativoException(Long id) {
        super("Veículo inativo com ID: " + id);
    }
}
