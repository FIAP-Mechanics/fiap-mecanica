package com.fiap.mecanica.atendimento.application.port.out;

import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.Status;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoGateway {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(String id);

    List<OrdemServico> buscarTodosPorStatusNotIn(List<Status> status);

    List<OrdemServico> buscarTodosPorStatusIn(List<Status> status);
}
