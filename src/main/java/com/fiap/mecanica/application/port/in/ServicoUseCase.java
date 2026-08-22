package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.domain.Servico;

public interface ServicoUseCase {
    Servico cadastrarServico(Servico servico);
    Servico buscarServicoPorId(Long idServico);
    Servico atualizarServico(Long idServico, AtualizarServicoCommand command);
    Servico excluirServico(Long idServico);
    Servico reativarServico(Long idServico);
}
