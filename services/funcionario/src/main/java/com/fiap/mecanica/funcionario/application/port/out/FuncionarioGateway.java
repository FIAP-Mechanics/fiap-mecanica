package com.fiap.mecanica.funcionario.application.port.out;

import com.fiap.mecanica.funcionario.domain.Funcionario;

import java.util.List;
import java.util.Optional;

public interface FuncionarioGateway {

    List<Funcionario> buscarTodos();

    Optional<Funcionario> buscarPorId(Long id);

    Optional<Funcionario> buscarPorEmail(String email);

    boolean existePorEmail(String email);

    Funcionario salvar(Funcionario funcionario);
}
