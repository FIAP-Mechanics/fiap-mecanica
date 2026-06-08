package com.fiap.mecanica.exception;

public class FuncionarioJaAtivoException extends RuntimeException {
    public FuncionarioJaAtivoException(Long id) {
        super("O Funcionário com id " + id + " já se encontra ativo.");
    }
}
