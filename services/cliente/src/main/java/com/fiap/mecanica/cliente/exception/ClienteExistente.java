package com.fiap.mecanica.cliente.exception;

public class ClienteExistente extends ConflitoException {
    public ClienteExistente(String documento) {
        super("Já existe um cliente com o documento: " + documento);
    }
}
