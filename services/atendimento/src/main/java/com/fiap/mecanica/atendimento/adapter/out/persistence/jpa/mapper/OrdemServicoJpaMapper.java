package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.OrcamentoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.OrdemServicoInsumoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.OrdemServicoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.OrdemServicoServicoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.TrocaStatusJpaEmbeddable;
import com.fiap.mecanica.atendimento.domain.Orcamento;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoInsumo;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.TrocaStatus;

import java.util.ArrayList;
import java.util.List;

public class OrdemServicoJpaMapper {

    private OrdemServicoJpaMapper() {
    }

    public static OrdemServico toDomain(OrdemServicoJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<TrocaStatus> historico = new ArrayList<>();
        if (entity.getHistoricoDeEventos() != null) {
            entity.getHistoricoDeEventos().forEach(evento -> historico.add(
                    TrocaStatus.builder()
                            .novoStatus(evento.getNovoStatus())
                            .dataHora(evento.getDataHora())
                            .build()));
        }

        OrdemServico ordemServico = OrdemServico.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .clienteId(entity.getClienteId())
                .veiculoId(entity.getVeiculoId())
                .relatoCliente(entity.getRelatoCliente())
                .observacoesDiagnostico(entity.getObservacoesDiagnostico())
                .dataCriacao(entity.getDataCriacao())
                .historicoDeEventos(historico)
                .build();

        if (entity.getOrcamento() != null) {
            ordemServico.setOrcamento(toDomain(entity.getOrcamento(), ordemServico));
        }

        return ordemServico;
    }

    private static Orcamento toDomain(OrcamentoJpaEntity entity, OrdemServico ordemServico) {
        Orcamento orcamento = Orcamento.builder()
                .id(entity.getId())
                .ordemServico(ordemServico)
                .precoTotal(entity.getPrecoTotal())
                .build();

        List<OrdemServicoServico> servicos = new ArrayList<>();
        if (entity.getServicos() != null) {
            entity.getServicos().forEach(item -> servicos.add(
                    OrdemServicoServico.builder()
                            .id(item.getId())
                            .orcamento(orcamento)
                            .servicoId(item.getServicoId())
                            .nomeServico(item.getNomeServico())
                            .valorUnitario(item.getValorUnitario())
                            .quantidade(item.getQuantidade())
                            .build()));
        }

        List<OrdemServicoInsumo> insumos = new ArrayList<>();
        if (entity.getInsumos() != null) {
            entity.getInsumos().forEach(item -> insumos.add(
                    OrdemServicoInsumo.builder()
                            .id(item.getId())
                            .orcamento(orcamento)
                            .insumoId(item.getInsumoId())
                            .nomeInsumo(item.getNomeInsumo())
                            .precoUnitario(item.getPrecoUnitario())
                            .quantidade(item.getQuantidade())
                            .build()));
        }

        orcamento.setServicos(servicos);
        orcamento.setInsumos(insumos);
        return orcamento;
    }

    public static OrdemServicoJpaEntity toJpaEntity(OrdemServico domain) {
        if (domain == null) {
            return null;
        }

        List<TrocaStatusJpaEmbeddable> historico = new ArrayList<>();
        if (domain.getHistoricoDeEventos() != null) {
            domain.getHistoricoDeEventos().forEach(evento -> historico.add(
                    TrocaStatusJpaEmbeddable.builder()
                            .novoStatus(evento.getNovoStatus())
                            .dataHora(evento.getDataHora())
                            .build()));
        }

        OrdemServicoJpaEntity entity = OrdemServicoJpaEntity.builder()
                .id(domain.getId())
                .status(domain.getStatus())
                .clienteId(domain.getClienteId())
                .veiculoId(domain.getVeiculoId())
                .relatoCliente(domain.getRelatoCliente())
                .observacoesDiagnostico(domain.getObservacoesDiagnostico())
                .dataCriacao(domain.getDataCriacao())
                .historicoDeEventos(historico)
                .build();

        if (domain.getOrcamento() != null) {
            entity.setOrcamento(toJpaEntity(domain.getOrcamento(), entity));
        }

        return entity;
    }

    private static OrcamentoJpaEntity toJpaEntity(Orcamento domain, OrdemServicoJpaEntity ordemServicoJpaEntity) {
        OrcamentoJpaEntity entity = OrcamentoJpaEntity.builder()
                .id(domain.getId())
                .ordemServico(ordemServicoJpaEntity)
                .precoTotal(domain.getPrecoTotal())
                .build();

        List<OrdemServicoServicoJpaEntity> servicos = new ArrayList<>();
        if (domain.getServicos() != null) {
            domain.getServicos().forEach(item -> servicos.add(
                    OrdemServicoServicoJpaEntity.builder()
                            .id(item.getId())
                            .orcamento(entity)
                            .servicoId(item.getServicoId())
                            .nomeServico(item.getNomeServico())
                            .valorUnitario(item.getValorUnitario())
                            .quantidade(item.getQuantidade())
                            .build()));
        }

        List<OrdemServicoInsumoJpaEntity> insumos = new ArrayList<>();
        if (domain.getInsumos() != null) {
            domain.getInsumos().forEach(item -> insumos.add(
                    OrdemServicoInsumoJpaEntity.builder()
                            .id(item.getId())
                            .orcamento(entity)
                            .insumoId(item.getInsumoId())
                            .nomeInsumo(item.getNomeInsumo())
                            .precoUnitario(item.getPrecoUnitario())
                            .quantidade(item.getQuantidade())
                            .build()));
        }

        entity.setServicos(servicos);
        entity.setInsumos(insumos);
        return entity;
    }
}
