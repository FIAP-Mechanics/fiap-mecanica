package com.fiap.mecanica.atendimento.adapter.in.web.presenter;

import com.fiap.mecanica.atendimento.adapter.in.web.response.OrcamentoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.OrdemServicoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.OrdemServicoInsumoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.OrdemServicoServicoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.TrocaStatusDto;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoInsumo;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.TempoMedioExecucaoServico;

public class AtendimentoPresenter {

    private AtendimentoPresenter() {
    }

    public static OrdemServicoDto toDto(OrdemServico ordemServico) {
        return OrdemServicoDto.builder()
                .id(ordemServico.getId())
                .status(ordemServico.getStatus())
                .descricaoStatus(ordemServico.getStatus() != null ? ordemServico.getStatus().getDescricao() : null)
                .clienteId(ordemServico.getClienteId())
                .veiculoId(ordemServico.getVeiculoId())
                .relatoCliente(ordemServico.getRelatoCliente())
                .observacoesDiagnostico(ordemServico.getObservacoesDiagnostico())
                .orcamento(toOrcamentoDto(ordemServico))
                .historicoDeEventos(ordemServico.getHistoricoDeEventos().stream()
                        .map(evento -> TrocaStatusDto.builder()
                                .status(evento.getNovoStatus())
                                .dataHora(evento.getDataHora())
                                .build())
                        .toList())
                .build();
    }

    public static TempoMedioExecucaoServicoDto toDto(TempoMedioExecucaoServico tempoMedioExecucaoServico) {
        return TempoMedioExecucaoServicoDto.builder()
                .servicoId(tempoMedioExecucaoServico.getServicoId())
                .nome(tempoMedioExecucaoServico.getNome())
                .ordensFinalizadas(tempoMedioExecucaoServico.getOrdensFinalizadas())
                .tempoMedioExecucaoMinutos(tempoMedioExecucaoServico.getTempoMedioExecucaoMinutos())
                .build();
    }

    private static OrcamentoDto toOrcamentoDto(OrdemServico ordemServico) {
        if (ordemServico.getOrcamento() == null) {
            return null;
        }

        return OrcamentoDto.builder()
                .servicos(ordemServico.getOrcamento().getServicos().stream()
                        .map(AtendimentoPresenter::toServicoDto)
                        .toList())
                .insumos(ordemServico.getOrcamento().getInsumos().stream()
                        .map(AtendimentoPresenter::toInsumoDto)
                        .toList())
                .precoTotal(ordemServico.getOrcamento().getPrecoTotal())
                .build();
    }

    private static OrdemServicoServicoDto toServicoDto(OrdemServicoServico item) {
        return OrdemServicoServicoDto.builder()
                .servicoId(item.getServicoId())
                .nomeServico(item.getNomeServico())
                .quantidade(item.getQuantidade())
                .valorUnitario(item.getValorUnitario())
                .build();
    }

    private static OrdemServicoInsumoDto toInsumoDto(OrdemServicoInsumo item) {
        return OrdemServicoInsumoDto.builder()
                .insumoId(item.getInsumoId())
                .nomeInsumo(item.getNomeInsumo())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .build();
    }
}
