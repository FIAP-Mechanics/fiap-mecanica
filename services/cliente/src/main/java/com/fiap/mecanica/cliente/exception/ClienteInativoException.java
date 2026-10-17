package com.fiap.mecanica.cliente.exception;

public class ClienteInativoException extends ValidacaoException {
    public ClienteInativoException(Long id) {
        super("O Cliente com ID " + id + " encontra-se inativo");
    }
}
