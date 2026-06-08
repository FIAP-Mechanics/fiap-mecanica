package com.fiap.mecanica.exception;

public class FuncionarioInativoException extends RuntimeException {
    public FuncionarioInativoException(Long id) {
        super("O Funcionário com ID " + id + " encontra-se inativo");
    }
}
