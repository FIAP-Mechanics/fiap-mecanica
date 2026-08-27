package com.fiap.mecanica.atendimento.application.port.in;

import com.fiap.mecanica.atendimento.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.atendimento.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.TempoMedioExecucaoServico;

import java.util.List;

public interface AtendimentoUseCase {

    OrdemServico iniciarAtendimento(Long clienteId, Long veiculoId, String relatoCliente,
                                     List<ServicoQuantidadeCommand> servicos, List<InsumoQuantidadeCommand> insumos);

    OrdemServico buscarPorId(String id);

    List<OrdemServico> listarAtendimentosEmAberto();

    List<TempoMedioExecucaoServico> listarTempoMedioExecucaoServicos();

    OrdemServico iniciarDiagnostico(String id);

    OrdemServico realizarDiagnostico(String id, List<ServicoQuantidadeCommand> servicos,
                                      List<InsumoQuantidadeCommand> insumos, String observacoesDiagnostico);

    OrdemServico aprovarOrdemServico(String id);

    OrdemServico cancelarOrdemServico(String id);

    OrdemServico finalizarOrdemServico(String id);

    OrdemServico entregarVeiculo(String id);
}
