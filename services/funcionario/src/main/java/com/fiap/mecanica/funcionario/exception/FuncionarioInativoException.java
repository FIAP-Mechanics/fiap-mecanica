package com.fiap.mecanica.funcionario.exception;

public class FuncionarioInativoException extends ValidacaoException {
    public FuncionarioInativoException(Long id) {
        super("O Funcionário com ID " + id + " encontra-se inativo");
    }
}
