package com.fiap.mecanica.cliente.exception;

public class ClienteJaAtivoException extends ValidacaoException {
    public ClienteJaAtivoException(Long id) {
        super("O Cliente com id " + id + " já se encontra ativo.");
    }
}
