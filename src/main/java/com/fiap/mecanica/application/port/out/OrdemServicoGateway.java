package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.Status;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoGateway {
    Optional<OrdemServico> buscarPorId(String id);
    List<OrdemServico> buscarPorStatusEm(List<Status> status);
    List<OrdemServico> buscarPorStatusForaDe(List<Status> status);
    OrdemServico salvar(OrdemServico ordemServico);
}
