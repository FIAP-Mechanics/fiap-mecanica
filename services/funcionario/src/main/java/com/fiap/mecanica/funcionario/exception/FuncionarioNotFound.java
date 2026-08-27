package com.fiap.mecanica.funcionario.exception;

public class FuncionarioNotFound extends EntidadeNaoEncontradaException {
    public FuncionarioNotFound(Long id) {
        super("Funcionário não encontrado com ID: " + id);
    }

    public FuncionarioNotFound(String email) {
        super("Funcionário não encontrado com e-mail: " + email);
    }
}
