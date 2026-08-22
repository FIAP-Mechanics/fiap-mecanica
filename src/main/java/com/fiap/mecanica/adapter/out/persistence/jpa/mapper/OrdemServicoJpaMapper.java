package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.OrcamentoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.OrdemServicoInsumoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.OrdemServicoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.OrdemServicoServicoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.TrocaStatusJpaEmbeddable;
import com.fiap.mecanica.domain.Orcamento;
import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.OrdemServicoInsumo;
import com.fiap.mecanica.domain.OrdemServicoServico;
import com.fiap.mecanica.domain.TrocaStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class OrdemServicoJpaMapper {

    private OrdemServicoJpaMapper() {
    }

    public static OrdemServicoJpaEntity toJpaEntity(OrdemServico ordemServico) {
        if (ordemServico == null) return null;

        OrdemServicoJpaEntity entity = OrdemServicoJpaEntity.builder()
                .id(ordemServico.getId())
                .status(ordemServico.getStatus())
                .cliente(ClienteJpaMapper.toJpaEntity(ordemServico.getCliente()))
                .veiculo(VeiculoJpaMapper.toJpaEntity(ordemServico.getVeiculo()))
                .relatoCliente(ordemServico.getRelatoCliente())
                .observacoesDiagnostico(ordemServico.getObservacoesDiagnostico())
                .historicoDeEventos(toJpaHistory(ordemServico.getHistoricoDeEventos()))
                .build();

        if (ordemServico.getOrcamento() != null) {
            entity.setOrcamento(toJpaEntity(ordemServico.getOrcamento(), entity));
        }
        return entity;
    }

    public static OrdemServico toDomain(OrdemServicoJpaEntity entity) {
        if (entity == null) return null;

        OrdemServico ordemServico = OrdemServico.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .cliente(ClienteJpaMapper.toDomain(entity.getCliente()))
                .veiculo(VeiculoJpaMapper.toDomain(entity.getVeiculo()))
                .relatoCliente(entity.getRelatoCliente())
                .observacoesDiagnostico(entity.getObservacoesDiagnostico())
                .historicoDeEventos(toDomainHistory(entity.getHistoricoDeEventos()))
                .build();

        if (entity.getOrcamento() != null) {
            ordemServico.setOrcamento(toDomain(entity.getOrcamento(), ordemServico));
        }
        return ordemServico;
    }

    private static OrcamentoJpaEntity toJpaEntity(
            Orcamento orcamento,
            OrdemServicoJpaEntity ordemServico) {
        OrcamentoJpaEntity entity = OrcamentoJpaEntity.builder()
                .id(orcamento.getId())
                .ordemServico(ordemServico)
                .precoTotal(orcamento.getPrecoTotal())
                .build();

        entity.setServicos(toJpaServicos(orcamento.getServicos(), entity));
        entity.setInsumos(toJpaInsumos(orcamento.getInsumos(), entity));
        return entity;
    }

    private static Orcamento toDomain(OrcamentoJpaEntity entity, OrdemServico ordemServico) {
        Orcamento orcamento = Orcamento.builder()
                .id(entity.getId())
                .ordemServico(ordemServico)
                .precoTotal(entity.getPrecoTotal())
                .build();

        orcamento.setServicos(toDomainServicos(entity.getServicos(), orcamento));
        orcamento.setInsumos(toDomainInsumos(entity.getInsumos(), orcamento));
        return orcamento;
    }

    private static List<OrdemServicoServicoJpaEntity> toJpaServicos(
            List<OrdemServicoServico> servicos,
            OrcamentoJpaEntity orcamento) {
        if (servicos == null) return null;
        return servicos.stream()
                .map(item -> OrdemServicoServicoJpaEntity.builder()
                        .id(item.getId())
                        .orcamento(orcamento)
                        .servico(ServicoJpaMapper.toJpaEntity(item.getServico()))
                        .quantidade(item.getQuantidade())
                        .tempoExecucaoMinutos(item.getTempoExecucaoMinutos())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<OrdemServicoInsumoJpaEntity> toJpaInsumos(
            List<OrdemServicoInsumo> insumos,
            OrcamentoJpaEntity orcamento) {
        if (insumos == null) return null;
        return insumos.stream()
                .map(item -> OrdemServicoInsumoJpaEntity.builder()
                        .id(item.getId())
                        .orcamento(orcamento)
                        .insumo(InsumoJpaMapper.toJpaEntity(item.getInsumo()))
                        .quantidade(item.getQuantidade())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<OrdemServicoServico> toDomainServicos(
            List<OrdemServicoServicoJpaEntity> servicos,
            Orcamento orcamento) {
        if (servicos == null) return null;
        return servicos.stream()
                .map(item -> OrdemServicoServico.builder()
                        .id(item.getId())
                        .orcamento(orcamento)
                        .servico(ServicoJpaMapper.toDomain(item.getServico()))
                        .quantidade(item.getQuantidade())
                        .tempoExecucaoMinutos(item.getTempoExecucaoMinutos())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<OrdemServicoInsumo> toDomainInsumos(
            List<OrdemServicoInsumoJpaEntity> insumos,
            Orcamento orcamento) {
        if (insumos == null) return null;
        return insumos.stream()
                .map(item -> OrdemServicoInsumo.builder()
                        .id(item.getId())
                        .orcamento(orcamento)
                        .insumo(InsumoJpaMapper.toDomain(item.getInsumo()))
                        .quantidade(item.getQuantidade())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<TrocaStatusJpaEmbeddable> toJpaHistory(List<TrocaStatus> historico) {
        if (historico == null) return null;
        return historico.stream()
                .map(evento -> TrocaStatusJpaEmbeddable.builder()
                        .novoStatus(evento.getNovoStatus())
                        .dataHora(evento.getDataHora())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<TrocaStatus> toDomainHistory(List<TrocaStatusJpaEmbeddable> historico) {
        if (historico == null) return null;
        return historico.stream()
                .map(evento -> TrocaStatus.builder()
                        .novoStatus(evento.getNovoStatus())
                        .dataHora(evento.getDataHora())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
