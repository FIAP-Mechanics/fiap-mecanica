package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.Servico;

import java.util.Optional;

public interface ServicoGateway {
    Optional<Servico> buscarPorId(Long id);
    Servico salvar(Servico servico);
}
