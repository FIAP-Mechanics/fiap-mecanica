package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.domain.*;
import com.fiap.mecanica.dto.OrcamentoDto;
import com.fiap.mecanica.dto.OrcamentoInsumoDto;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.dto.OrdemServicoServicoDto;
import com.fiap.mecanica.dto.TrocaStatusDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrdemServicoMapper {

    private OrdemServicoMapper() {
    }

    public static OrdemServico toEntity(Cliente cliente, Veiculo veiculo, String relatoCliente) {
        OrdemServico ordemServico = OrdemServico.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .relatoCliente(relatoCliente)
                .build();
        ordemServico.setStatus(Status.RECEBIDA);
        return ordemServico;
    }

    public static OrdemServicoServico toServicoEntity(Orcamento orcamento, Servico servico, Integer quantidade) {
        return OrdemServicoServico.builder()
                .orcamento(orcamento)
                .servico(servico)
                .quantidade(quantidade)
                .build();
    }

    public static OrdemServicoInsumo toInsumoEntity(Orcamento orcamento, Insumo insumo, Integer quantidade) {
        return OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumo(insumo)
                .quantidade(quantidade)
                .build();
    }

    public static OrdemServicoServicoDto toServicoDto(OrdemServicoServico ordemServicoServico) {
        Servico servico = ordemServicoServico.getServico();
        return OrdemServicoServicoDto.builder()
                .servicoId(servico.getId())
                .nome(servico.getNome())
                .valorUnitario(servico.getValor())
                .quantidade(ordemServicoServico.getQuantidade())
                .tempoExecucaoMinutos(ordemServicoServico.getTempoExecucaoMinutos())
                .valorTotal(servico.getValor()
                        .multiply(java.math.BigDecimal.valueOf(ordemServicoServico.getQuantidade())))
                .build();
    }

    public static OrcamentoInsumoDto toInsumoDto(OrdemServicoInsumo ordemServicoInsumo) {
        Insumo insumo = ordemServicoInsumo.getInsumo();
        return OrcamentoInsumoDto.builder()
                .insumoId(insumo.getId())
                .nome(insumo.getNome())
                .precoUnitario(insumo.getPrecoUnitario())
                .quantidade(ordemServicoInsumo.getQuantidade())
                .valorTotal(insumo.getPrecoUnitario()
                        .multiply(java.math.BigDecimal.valueOf(ordemServicoInsumo.getQuantidade())))
                .build();
    }

    public static OrcamentoDto toOrcamentoDto(Orcamento orcamento) {
        if (orcamento == null) {
            return null;
        }

        List<OrdemServicoServicoDto> servicosDto = orcamento.getServicos() != null
                ? orcamento.getServicos().stream()
                .map(OrdemServicoMapper::toServicoDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<OrcamentoInsumoDto> insumosDto = orcamento.getInsumos() != null
                ? orcamento.getInsumos().stream()
                .map(OrdemServicoMapper::toInsumoDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return OrcamentoDto.builder()
                .id(orcamento.getId())
                .servicos(servicosDto)
                .insumos(insumosDto)
                .precoTotal(orcamento.getPrecoTotal())
                .build();
    }

    public static TrocaStatusDto toTrocaStatusDto(TrocaStatus trocaStatus) {
        return TrocaStatusDto.builder()
                .status(trocaStatus.getStatus())
                .dataHora(trocaStatus.getDataHora())
                .build();
    }

    public static OrdemServicoDto toDto(OrdemServico ordemServico) {
        List<TrocaStatusDto> historico = ordemServico.getHistoricoDeEventos() != null
                ? ordemServico.getHistoricoDeEventos().stream()
                .map(OrdemServicoMapper::toTrocaStatusDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return OrdemServicoDto.builder()
                .id(ordemServico.getId())
                .status(ordemServico.getStatus())
                .descricaoStatus(ordemServico.getStatus().getDescricao())
                .cliente(ClienteMapper.toDto(ordemServico.getCliente()))
                .veiculo(VeiculoMapper.toDto(ordemServico.getVeiculo()))
                .relatoCliente(ordemServico.getRelatoCliente())
                .observacoesDiagnostico(ordemServico.getObservacoesDiagnostico())
                .orcamento(toOrcamentoDto(ordemServico.getOrcamento()))
                .historicoDeEventos(historico)
                .build();
    }
}
