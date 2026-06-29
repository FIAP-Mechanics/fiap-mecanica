package com.fiap.mecanica.exception;

public class VeiculoJaCadastradoException extends ConflitoException {

    public VeiculoJaCadastradoException(String placa) {
        super("Já existe um veículo cadastrado com a placa: " + placa);
    }
}