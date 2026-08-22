package com.fiap.mecanica.application.port.in;

import com.fiap.mecanica.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoTempoCommand;
import com.fiap.mecanica.application.result.TempoMedioExecucaoServicoResult;
import com.fiap.mecanica.domain.OrdemServico;

import java.util.List;

public interface AtendimentoUseCase {
    OrdemServico iniciarAtendimento(
            Long clienteId,
            Long veiculoId,
            String relatoCliente,
            List<ServicoQuantidadeCommand> servicos,
            List<InsumoQuantidadeCommand> insumos);
    OrdemServico buscarPorId(String id);
    List<OrdemServico> listarAtendimentosEmAberto();
    List<TempoMedioExecucaoServicoResult> listarTempoMedioExecucaoServicos();
    OrdemServico iniciarDiagnostico(String id);
    OrdemServico realizarDiagnostico(
            String id,
            List<ServicoQuantidadeCommand> servicos,
            List<InsumoQuantidadeCommand> insumos,
            String observacoesDiagnostico);
    OrdemServico aprovarOrdemServico(String id);
    OrdemServico cancelarOrdemServico(String id);
    OrdemServico finalizarOrdemServico(String id, List<ServicoTempoCommand> servicosTempo);
    OrdemServico entregarVeiculo(String id);
}
