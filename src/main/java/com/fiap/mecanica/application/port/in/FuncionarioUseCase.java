package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.domain.Funcionario;

import java.util.Optional;

public interface FuncionarioUseCase {
    Funcionario cadastrarFuncionario(Funcionario funcionario);
    Funcionario buscarFuncionarioPorId(Long id);
    Optional<Funcionario> buscarFuncionarioPorEmail(String email);
    Funcionario atualizarFuncionario(Long id, AtualizarFuncionarioCommand command);
    Funcionario excluirFuncionario(Long id);
    Funcionario reativarFuncionario(Long id);
}
