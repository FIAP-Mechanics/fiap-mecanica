package com.fiap.mecanica.atendimento.application.port.out;

import com.fiap.mecanica.atendimento.domain.Funcionario;

import java.util.Optional;

public interface FuncionarioGateway {

    Optional<Funcionario> buscarPorEmail(String email);

    Funcionario salvar(Funcionario funcionario);
}
