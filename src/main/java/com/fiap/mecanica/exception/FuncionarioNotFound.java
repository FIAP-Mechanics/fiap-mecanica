package com.fiap.mecanica.exception;

public class FuncionarioNotFound extends RuntimeException {
    public FuncionarioNotFound(Long id) {
        super("Funcionário não encontrado com ID: " + id);
    }
}
