package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.*;
import com.fiap.mecanica.atendimento.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoJpaMapperTest {

    private static final String ID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final String RELATO_CLIENTE = "Nao liga";
    private static final String OBSERVACOES_DIAGNOSTICO = "Bateria fraca";
    private static final Long ID_ORCAMENTO = 10L;
    private static final Long ID_ITEM_SERVICO = 20L;
    private static final Long ID_SERVICO = 30L;
    private static final String NOME_SERVICO = "Troca de bateria";
    private static final BigDecimal VALOR_UNITARIO_SERVICO = BigDecimal.valueOf(100);
    private static final Long ID_ITEM_INSUMO = 40L;
    private static final Long ID_INSUMO = 50L;
    private static final String NOME_INSUMO = "Bateria";
    private static final BigDecimal PRECO_UNITARIO_INSUMO = BigDecimal.valueOf(200);
    private static final Integer QUANTIDADE = 1;
    private static final BigDecimal PRECO_TOTAL = BigDecimal.valueOf(300);
    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2024, 1, 1, 10, 0);

    // ===================== toDomain =====================

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(OrdemServicoJpaMapper.toDomain(null)).isNull();
    }

    @Test
    void deveConverterEntitySemOrcamentoParaDomain() {
        OrdemServicoJpaEntity entity = criarEntity(null);

        OrdemServico resultado = OrdemServicoJpaMapper.toDomain(entity);

        assertThat(resultado.getId()).isEqualTo(ID_ORDEM);
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getClienteId()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(resultado.getRelatoCliente()).isEqualTo(RELATO_CLIENTE);
        assertThat(resultado.getObservacoesDiagnostico()).isEqualTo(OBSERVACOES_DIAGNOSTICO);
        assertThat(resultado.getHistoricoDeEventos()).hasSize(1);
        assertThat(resultado.getHistoricoDeEventos().getFirst().getNovoStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getHistoricoDeEventos().getFirst().getDataHora()).isEqualTo(DATA_HORA);
        assertThat(resultado.getOrcamento()).isNull();
    }

    @Test
    void deveConverterEntityComOrcamentoReconstruindoReferenciasBidirecionais() {
        OrcamentoJpaEntity orcamentoEntity = criarOrcamentoEntity();
        OrdemServicoJpaEntity entity = criarEntity(orcamentoEntity);

        OrdemServico resultado = OrdemServicoJpaMapper.toDomain(entity);

        Orcamento orcamento = resultado.getOrcamento();
        assertThat(orcamento).isNotNull();
        assertThat(orcamento.getId()).isEqualTo(ID_ORCAMENTO);
        assertThat(orcamento.getOrdemServico()).isSameAs(resultado);
        assertThat(orcamento.getPrecoTotal()).isEqualTo(PRECO_TOTAL);

        assertThat(orcamento.getServicos()).hasSize(1);
        OrdemServicoServico servico = orcamento.getServicos().getFirst();
        assertThat(servico.getId()).isEqualTo(ID_ITEM_SERVICO);
        assertThat(servico.getOrcamento()).isSameAs(orcamento);
        assertThat(servico.getServicoId()).isEqualTo(ID_SERVICO);
        assertThat(servico.getNomeServico()).isEqualTo(NOME_SERVICO);
        assertThat(servico.getValorUnitario()).isEqualTo(VALOR_UNITARIO_SERVICO);
        assertThat(servico.getQuantidade()).isEqualTo(QUANTIDADE);

        assertThat(orcamento.getInsumos()).hasSize(1);
        OrdemServicoInsumo insumo = orcamento.getInsumos().getFirst();
        assertThat(insumo.getId()).isEqualTo(ID_ITEM_INSUMO);
        assertThat(insumo.getOrcamento()).isSameAs(orcamento);
        assertThat(insumo.getInsumoId()).isEqualTo(ID_INSUMO);
        assertThat(insumo.getNomeInsumo()).isEqualTo(NOME_INSUMO);
        assertThat(insumo.getPrecoUnitario()).isEqualTo(PRECO_UNITARIO_INSUMO);
        assertThat(insumo.getQuantidade()).isEqualTo(QUANTIDADE);
    }

    @Test
    void deveConverterEntityComOrcamentoSemServicosNemInsumosParaListasVazias() {
        OrcamentoJpaEntity orcamentoEntity = OrcamentoJpaEntity.builder()
                .id(ID_ORCAMENTO)
                .precoTotal(PRECO_TOTAL)
                .servicos(null)
                .insumos(null)
                .build();
        OrdemServicoJpaEntity entity = criarEntity(orcamentoEntity);

        OrdemServico resultado = OrdemServicoJpaMapper.toDomain(entity);

        assertThat(resultado.getOrcamento().getServicos()).isEmpty();
        assertThat(resultado.getOrcamento().getInsumos()).isEmpty();
    }

    @Test
    void deveConverterEntityComHistoricoNuloParaListaVazia() {
        OrdemServicoJpaEntity entity = criarEntity(null);
        entity.setHistoricoDeEventos(null);

        OrdemServico resultado = OrdemServicoJpaMapper.toDomain(entity);

        assertThat(resultado.getHistoricoDeEventos()).isEmpty();
    }

    // ===================== toJpaEntity =====================

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(OrdemServicoJpaMapper.toJpaEntity(null)).isNull();
    }

    @Test
    void deveConverterDomainSemOrcamentoParaEntity() {
        OrdemServico domain = criarDomain(null);

        OrdemServicoJpaEntity resultado = OrdemServicoJpaMapper.toJpaEntity(domain);

        assertThat(resultado.getId()).isEqualTo(ID_ORDEM);
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getClienteId()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(resultado.getRelatoCliente()).isEqualTo(RELATO_CLIENTE);
        assertThat(resultado.getObservacoesDiagnostico()).isEqualTo(OBSERVACOES_DIAGNOSTICO);
        assertThat(resultado.getHistoricoDeEventos()).hasSize(1);
        assertThat(resultado.getHistoricoDeEventos().getFirst().getNovoStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getOrcamento()).isNull();
    }

    @Test
    void deveConverterDomainComOrcamentoReconstruindoReferenciasBidirecionais() {
        Orcamento orcamento = criarOrcamentoDomain();
        OrdemServico domain = criarDomain(orcamento);

        OrdemServicoJpaEntity resultado = OrdemServicoJpaMapper.toJpaEntity(domain);

        OrcamentoJpaEntity orcamentoEntity = resultado.getOrcamento();
        assertThat(orcamentoEntity).isNotNull();
        assertThat(orcamentoEntity.getId()).isEqualTo(ID_ORCAMENTO);
        assertThat(orcamentoEntity.getOrdemServico()).isSameAs(resultado);
        assertThat(orcamentoEntity.getPrecoTotal()).isEqualTo(PRECO_TOTAL);

        assertThat(orcamentoEntity.getServicos()).hasSize(1);
        OrdemServicoServicoJpaEntity servicoEntity = orcamentoEntity.getServicos().getFirst();
        assertThat(servicoEntity.getId()).isEqualTo(ID_ITEM_SERVICO);
        assertThat(servicoEntity.getOrcamento()).isSameAs(orcamentoEntity);
        assertThat(servicoEntity.getServicoId()).isEqualTo(ID_SERVICO);
        assertThat(servicoEntity.getNomeServico()).isEqualTo(NOME_SERVICO);
        assertThat(servicoEntity.getValorUnitario()).isEqualTo(VALOR_UNITARIO_SERVICO);
        assertThat(servicoEntity.getQuantidade()).isEqualTo(QUANTIDADE);

        assertThat(orcamentoEntity.getInsumos()).hasSize(1);
        OrdemServicoInsumoJpaEntity insumoEntity = orcamentoEntity.getInsumos().getFirst();
        assertThat(insumoEntity.getId()).isEqualTo(ID_ITEM_INSUMO);
        assertThat(insumoEntity.getOrcamento()).isSameAs(orcamentoEntity);
        assertThat(insumoEntity.getInsumoId()).isEqualTo(ID_INSUMO);
        assertThat(insumoEntity.getNomeInsumo()).isEqualTo(NOME_INSUMO);
        assertThat(insumoEntity.getPrecoUnitario()).isEqualTo(PRECO_UNITARIO_INSUMO);
        assertThat(insumoEntity.getQuantidade()).isEqualTo(QUANTIDADE);
    }

    @Test
    void deveConverterDomainComOrcamentoSemServicosNemInsumosParaListasVazias() {
        Orcamento orcamento = Orcamento.builder()
                .id(ID_ORCAMENTO)
                .precoTotal(PRECO_TOTAL)
                .servicos(null)
                .insumos(null)
                .build();
        OrdemServico domain = criarDomain(orcamento);

        OrdemServicoJpaEntity resultado = OrdemServicoJpaMapper.toJpaEntity(domain);

        assertThat(resultado.getOrcamento().getServicos()).isEmpty();
        assertThat(resultado.getOrcamento().getInsumos()).isEmpty();
    }

    @Test
    void deveConverterDomainComHistoricoNuloParaListaVazia() {
        OrdemServico domain = OrdemServico.builder()
                .id(ID_ORDEM)
                .status(Status.RECEBIDA)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .relatoCliente(RELATO_CLIENTE)
                .observacoesDiagnostico(OBSERVACOES_DIAGNOSTICO)
                .historicoDeEventos(null)
                .orcamento(null)
                .build();

        OrdemServicoJpaEntity resultado = OrdemServicoJpaMapper.toJpaEntity(domain);

        assertThat(resultado.getHistoricoDeEventos()).isEmpty();
    }

    private OrdemServicoJpaEntity criarEntity(OrcamentoJpaEntity orcamento) {
        return OrdemServicoJpaEntity.builder()
                .id(ID_ORDEM)
                .status(Status.RECEBIDA)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .relatoCliente(RELATO_CLIENTE)
                .observacoesDiagnostico(OBSERVACOES_DIAGNOSTICO)
                .historicoDeEventos(List.of(TrocaStatusJpaEmbeddable.builder()
                        .novoStatus(Status.RECEBIDA)
                        .dataHora(DATA_HORA)
                        .build()))
                .orcamento(orcamento)
                .build();
    }

    private OrcamentoJpaEntity criarOrcamentoEntity() {
        OrcamentoJpaEntity orcamento = OrcamentoJpaEntity.builder()
                .id(ID_ORCAMENTO)
                .precoTotal(PRECO_TOTAL)
                .build();

        orcamento.setServicos(List.of(OrdemServicoServicoJpaEntity.builder()
                .id(ID_ITEM_SERVICO)
                .servicoId(ID_SERVICO)
                .nomeServico(NOME_SERVICO)
                .valorUnitario(VALOR_UNITARIO_SERVICO)
                .quantidade(QUANTIDADE)
                .build()));

        orcamento.setInsumos(List.of(OrdemServicoInsumoJpaEntity.builder()
                .id(ID_ITEM_INSUMO)
                .insumoId(ID_INSUMO)
                .nomeInsumo(NOME_INSUMO)
                .precoUnitario(PRECO_UNITARIO_INSUMO)
                .quantidade(QUANTIDADE)
                .build()));

        return orcamento;
    }

    private OrdemServico criarDomain(Orcamento orcamento) {
        List<TrocaStatus> historico = new java.util.ArrayList<>();
        historico.add(TrocaStatus.builder()
                .novoStatus(Status.RECEBIDA)
                .dataHora(DATA_HORA)
                .build());

        return OrdemServico.builder()
                .id(ID_ORDEM)
                .status(Status.RECEBIDA)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .relatoCliente(RELATO_CLIENTE)
                .observacoesDiagnostico(OBSERVACOES_DIAGNOSTICO)
                .historicoDeEventos(historico)
                .orcamento(orcamento)
                .build();
    }

    private Orcamento criarOrcamentoDomain() {
        Orcamento orcamento = Orcamento.builder()
                .id(ID_ORCAMENTO)
                .precoTotal(PRECO_TOTAL)
                .build();

        orcamento.setServicos(List.of(OrdemServicoServico.builder()
                .id(ID_ITEM_SERVICO)
                .servicoId(ID_SERVICO)
                .nomeServico(NOME_SERVICO)
                .valorUnitario(VALOR_UNITARIO_SERVICO)
                .quantidade(QUANTIDADE)
                .build()));

        orcamento.setInsumos(List.of(OrdemServicoInsumo.builder()
                .id(ID_ITEM_INSUMO)
                .insumoId(ID_INSUMO)
                .nomeInsumo(NOME_INSUMO)
                .precoUnitario(PRECO_UNITARIO_INSUMO)
                .quantidade(QUANTIDADE)
                .build()));

        return orcamento;
    }
}
