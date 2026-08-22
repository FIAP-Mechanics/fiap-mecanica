package com.fiap.mecanica.funcionario.exception;

public class FuncionarioJaAtivoException extends ValidacaoException {
    public FuncionarioJaAtivoException(Long id) {
        super("O Funcionário com id " + id + " já se encontra ativo.");
    }
}
