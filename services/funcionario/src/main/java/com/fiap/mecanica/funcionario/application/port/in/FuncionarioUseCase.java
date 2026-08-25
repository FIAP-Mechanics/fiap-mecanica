package com.fiap.mecanica.funcionario.application.port.in;

import com.fiap.mecanica.funcionario.application.command.AtualizarFuncionarioCommand;
import com.fiap.mecanica.funcionario.domain.Funcionario;

import java.util.List;

public interface FuncionarioUseCase {

    List<Funcionario> buscarTodos();

    Funcionario cadastrarFuncionario(Funcionario funcionario);

    Funcionario buscarFuncionarioPorId(Long id);

    Funcionario buscarPorEmail(String email);

    Funcionario atualizarFuncionario(Long id, AtualizarFuncionarioCommand command);

    Funcionario excluirFuncionario(Long id);

    Funcionario reativarFuncionario(Long id);
}
