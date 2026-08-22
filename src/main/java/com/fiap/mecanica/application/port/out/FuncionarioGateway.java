package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.Funcionario;

import java.util.Optional;

public interface FuncionarioGateway {
    Optional<Funcionario> buscarPorId(Long id);
    Optional<Funcionario> buscarPorEmail(String email);
    Funcionario salvar(Funcionario funcionario);
}
