package com.fiap.mecanica.servico.application.port.in;

import com.fiap.mecanica.servico.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.servico.domain.Servico;

import java.util.List;

public interface ServicoUseCase {

    List<Servico> buscarTodos();

    Servico cadastrarServico(Servico servico);

    Servico buscarServicoPorId(Long idServico);

    Servico atualizarServico(Long idServico, AtualizarServicoCommand command);

    Servico excluirServico(Long idServico);

    Servico reativarServico(Long idServico);
}
