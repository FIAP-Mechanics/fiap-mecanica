package com.fiap.mecanica.servico.application.port.out;

import com.fiap.mecanica.servico.domain.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoGateway {

    List<Servico> buscarTodos();

    Optional<Servico> buscarPorId(Long id);

    Servico salvar(Servico servico);
}
