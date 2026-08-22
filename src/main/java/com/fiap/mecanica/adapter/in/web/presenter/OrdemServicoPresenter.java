package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.adapter.in.web.request.FinalizarOrdemServicoRequest;
import com.fiap.mecanica.adapter.in.web.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.adapter.in.web.response.OrcamentoDto;
import com.fiap.mecanica.adapter.in.web.response.OrcamentoInsumoDto;
import com.fiap.mecanica.adapter.in.web.response.OrdemServicoDto;
import com.fiap.mecanica.adapter.in.web.response.OrdemServicoServicoDto;
import com.fiap.mecanica.adapter.in.web.response.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.adapter.in.web.response.TrocaStatusDto;
import com.fiap.mecanica.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoTempoCommand;
import com.fiap.mecanica.application.result.TempoMedioExecucaoServicoResult;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.Orcamento;
import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.OrdemServicoInsumo;
import com.fiap.mecanica.domain.OrdemServicoServico;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.domain.TrocaStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class OrdemServicoPresenter {

    private OrdemServicoPresenter() {
    }

    public static List<ServicoQuantidadeCommand> toServicoCommands(IniciarAtendimentoRequest request) {
        return request.servicos() == null ? null : request.servicos().stream()
                .map(item -> new ServicoQuantidadeCommand(item.servico(), item.quantidade()))
                .toList();
    }

    public static List<InsumoQuantidadeCommand> toInsumoCommands(IniciarAtendimentoRequest request) {
        return request.insumos() == null ? null : request.insumos().stream()
                .map(item -> new InsumoQuantidadeCommand(item.insumo(), item.quantidade()))
                .toList();
    }

    public static List<ServicoQuantidadeCommand> toServicoCommands(AdicionarItensOrcamentoRequest request) {
        return request.servicos() == null ? null : request.servicos().stream()
                .map(item -> new ServicoQuantidadeCommand(item.servico(), item.quantidade()))
                .toList();
    }

    public static List<InsumoQuantidadeCommand> toInsumoCommands(AdicionarItensOrcamentoRequest request) {
        return request.insumos() == null ? null : request.insumos().stream()
                .map(item -> new InsumoQuantidadeCommand(item.insumo(), item.quantidade()))
                .toList();
    }

    public static List<ServicoTempoCommand> toServicoTempoCommands(FinalizarOrdemServicoRequest request) {
        return request.servicos() == null ? null : request.servicos().stream()
                .map(item -> new ServicoTempoCommand(item.servico(), item.tempoGastoMinutos()))
                .toList();
    }

    public static TempoMedioExecucaoServicoDto toDto(TempoMedioExecucaoServicoResult result) {
        return TempoMedioExecucaoServicoDto.builder()
                .servicoId(result.servicoId())
                .nome(result.nome())
                .ordensFinalizadas(result.ordensFinalizadas())
                .tempoMedioExecucaoMinutos(result.tempoMedioExecucaoMinutos())
                .build();
    }

    public static OrdemServicoServicoDto toServicoDto(OrdemServicoServico item) {
        Servico servico = item.getServico();
        return OrdemServicoServicoDto.builder()
                .servicoId(servico.getId())
                .nome(servico.getNome())
                .valorUnitario(servico.getValor())
                .quantidade(item.getQuantidade())
                .tempoExecucaoMinutos(item.getTempoExecucaoMinutos())
                .valorTotal(servico.getValor().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .build();
    }

    public static OrcamentoInsumoDto toInsumoDto(OrdemServicoInsumo item) {
        Insumo insumo = item.getInsumo();
        return OrcamentoInsumoDto.builder()
                .insumoId(insumo.getId())
                .nome(insumo.getNome())
                .precoUnitario(insumo.getPrecoUnitario())
                .quantidade(item.getQuantidade())
                .valorTotal(insumo.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .build();
    }

    public static OrcamentoDto toOrcamentoDto(Orcamento orcamento) {
        if (orcamento == null) {
            return null;
        }
        List<OrdemServicoServicoDto> servicos = orcamento.getServicos() == null
                ? Collections.emptyList()
                : orcamento.getServicos().stream()
                        .map(OrdemServicoPresenter::toServicoDto)
                        .collect(Collectors.toList());
        List<OrcamentoInsumoDto> insumos = orcamento.getInsumos() == null
                ? Collections.emptyList()
                : orcamento.getInsumos().stream()
                        .map(OrdemServicoPresenter::toInsumoDto)
                        .collect(Collectors.toList());
        return OrcamentoDto.builder()
                .id(orcamento.getId())
                .servicos(servicos)
                .insumos(insumos)
                .precoTotal(orcamento.getPrecoTotal())
                .build();
    }

    public static TrocaStatusDto toTrocaStatusDto(TrocaStatus evento) {
        return TrocaStatusDto.builder()
                .status(evento.getNovoStatus())
                .dataHora(evento.getDataHora())
                .build();
    }

    public static OrdemServicoDto toDto(OrdemServico ordemServico) {
        List<TrocaStatusDto> historico = ordemServico.getHistoricoDeEventos() == null
                ? Collections.emptyList()
                : ordemServico.getHistoricoDeEventos().stream()
                        .map(OrdemServicoPresenter::toTrocaStatusDto)
                        .collect(Collectors.toList());
        return OrdemServicoDto.builder()
                .id(ordemServico.getId())
                .status(ordemServico.getStatus())
                .descricaoStatus(ordemServico.getStatus().getDescricao())
                .cliente(ClientePresenter.toDto(ordemServico.getCliente()))
                .veiculo(VeiculoPresenter.toDto(ordemServico.getVeiculo()))
                .relatoCliente(ordemServico.getRelatoCliente())
                .observacoesDiagnostico(ordemServico.getObservacoesDiagnostico())
                .orcamento(toOrcamentoDto(ordemServico.getOrcamento()))
                .historicoDeEventos(historico)
                .build();
    }
}
