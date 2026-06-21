package com.fiap.mecanica.exception;

public class ClienteExistente extends RuntimeException {
    public ClienteExistente(String documento) {
        super("Já existe um cliente com o documento: " + documento);
    }
}
