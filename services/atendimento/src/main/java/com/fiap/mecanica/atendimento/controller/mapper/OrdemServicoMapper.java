package com.fiap.mecanica.atendimento.controller.mapper;

import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoInsumo;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.dto.OrcamentoDto;
import com.fiap.mecanica.atendimento.dto.OrdemServicoDto;
import com.fiap.mecanica.atendimento.dto.OrdemServicoInsumoDto;
import com.fiap.mecanica.atendimento.dto.OrdemServicoServicoDto;
import com.fiap.mecanica.atendimento.dto.TrocaStatusDto;

public class OrdemServicoMapper {

    private OrdemServicoMapper() {
    }

    public static OrdemServico toEntity(Long clienteId, Long veiculoId, String relatoCliente) {
        return OrdemServico.builder()
                .status(Status.RECEBIDA)
                .clienteId(clienteId)
                .veiculoId(veiculoId)
                .relatoCliente(relatoCliente)
                .build();
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

    private static OrcamentoDto toOrcamentoDto(OrdemServico ordemServico) {
        if (ordemServico.getOrcamento() == null) {
            return null;
        }

        return OrcamentoDto.builder()
                .servicos(ordemServico.getOrcamento().getServicos().stream()
                        .map(OrdemServicoMapper::toServicoDto)
                        .toList())
                .insumos(ordemServico.getOrcamento().getInsumos().stream()
                        .map(OrdemServicoMapper::toInsumoDto)
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
