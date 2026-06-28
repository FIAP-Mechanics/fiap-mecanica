package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.domain.*;
import com.fiap.mecanica.dto.OrcamentoDto;
import com.fiap.mecanica.dto.OrcamentoInsumoDto;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.dto.OrdemServicoServicoDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoMapperTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final Long ID_SERVICO = 3L;
    private static final Long ID_INSUMO = 4L;
    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RELATO = "Problema relatado pelo cliente";

    // ===================== toEntity =====================

    @Test
    void deveConverterParaEntidadeComStatusRecebida() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        String relato = "Problema no motor";

        OrdemServico resultado = OrdemServicoMapper.toEntity(cliente, veiculo, relato);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCliente()).isEqualTo(cliente);
        assertThat(resultado.getVeiculo()).isEqualTo(veiculo);
        assertThat(resultado.getRelatoCliente()).isEqualTo(relato);
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getId()).isNull();
    }

    // ===================== toServicoEntity =====================

    @Test
    void deveConverterServicoQuantidadeParaEntidade() {
        Orcamento orcamento = criarOrcamento();
        Servico servico = criarServico();

        OrdemServicoServico resultado = OrdemServicoMapper.toServicoEntity(orcamento, servico, 2);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getOrcamento()).isEqualTo(orcamento);
        assertThat(resultado.getServico()).isEqualTo(servico);
        assertThat(resultado.getQuantidade()).isEqualTo(2);
    }

    // ===================== toInsumoEntity =====================

    @Test
    void deveConverterInsumoQuantidadeParaEntidade() {
        Orcamento orcamento = criarOrcamento();
        Insumo insumo = criarInsumo();

        OrdemServicoInsumo resultado = OrdemServicoMapper.toInsumoEntity(orcamento, insumo, 3);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getOrcamento()).isEqualTo(orcamento);
        assertThat(resultado.getInsumo()).isEqualTo(insumo);
        assertThat(resultado.getQuantidade()).isEqualTo(3);
    }

    // ===================== toServicoDto =====================

    @Test
    void deveConverterOrdemServicoServicoParaDto() {
        Servico servico = criarServico();
        OrdemServicoServico ordemServicoServico = OrdemServicoServico.builder()
                .servico(servico)
                .quantidade(2)
                .tempoExecucaoMinutos(90L)
                .build();

        OrdemServicoServicoDto resultado = OrdemServicoMapper.toServicoDto(ordemServicoServico);

        assertThat(resultado).isNotNull();
        assertThat(resultado.servicoId()).isEqualTo(ID_SERVICO);
        assertThat(resultado.nome()).isEqualTo("Troca de óleo");
        assertThat(resultado.valorUnitario()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(resultado.quantidade()).isEqualTo(2);
        assertThat(resultado.tempoExecucaoMinutos()).isEqualTo(90L);
        assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    // ===================== toInsumoDto =====================

    @Test
    void deveConverterOrdemServicoInsumoParaDto() {
        Insumo insumo = criarInsumo();
        OrdemServicoInsumo ordemServicoInsumo = OrdemServicoInsumo.builder()
                .insumo(insumo)
                .quantidade(2)
                .build();

        OrcamentoInsumoDto resultado = OrdemServicoMapper.toInsumoDto(ordemServicoInsumo);

        assertThat(resultado).isNotNull();
        assertThat(resultado.insumoId()).isEqualTo(ID_INSUMO);
        assertThat(resultado.nome()).isEqualTo("Óleo de motor");
        assertThat(resultado.precoUnitario()).isEqualByComparingTo(new BigDecimal("45.90"));
        assertThat(resultado.quantidade()).isEqualTo(2);
        assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("91.80"));
    }

    // ===================== toOrcamentoDto =====================

    @Test
    void deveRetornarNullQuandoOrcamentoForNull() {
        OrcamentoDto resultado = OrdemServicoMapper.toOrcamentoDto(null);

        assertThat(resultado).isNull();
    }

    @Test
    void deveConverterOrcamentoParaDtoComServicosEInsumos() {
        Servico servico = criarServico();
        Insumo insumo = criarInsumo();
        Orcamento orcamento = criarOrcamento();

        OrdemServicoServico ordemServicoServico = OrdemServicoServico.builder()
                .orcamento(orcamento)
                .servico(servico)
                .quantidade(1)
                .build();
        OrdemServicoInsumo ordemServicoInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumo(insumo)
                .quantidade(2)
                .build();

        orcamento.setServicos(List.of(ordemServicoServico));
        orcamento.setInsumos(List.of(ordemServicoInsumo));
        orcamento.recalcularPrecoTotal();

        OrcamentoDto resultado = OrdemServicoMapper.toOrcamentoDto(orcamento);

        assertThat(resultado).isNotNull();
        assertThat(resultado.servicos()).hasSize(1);
        assertThat(resultado.servicos().getFirst().servicoId()).isEqualTo(ID_SERVICO);
        assertThat(resultado.insumos()).hasSize(1);
        assertThat(resultado.insumos().getFirst().insumoId()).isEqualTo(ID_INSUMO);
        assertThat(resultado.precoTotal()).isEqualByComparingTo(new BigDecimal("241.80"));
    }

    // ===================== toDto =====================

    @Test
    void deveConverterParaDtoSemOrcamento() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .cliente(cliente)
                .veiculo(veiculo)
                .relatoCliente(RELATO)
                .status(Status.RECEBIDA)
                .build();

        OrdemServicoDto resultado = OrdemServicoMapper.toDto(ordemServico);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.descricaoStatus()).isEqualTo(Status.RECEBIDA.getDescricao());
        assertThat(resultado.cliente()).isNotNull();
        assertThat(resultado.cliente().id()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.veiculo()).isNotNull();
        assertThat(resultado.veiculo().id()).isEqualTo(ID_VEICULO);
        assertThat(resultado.relatoCliente()).isEqualTo(RELATO);
        assertThat(resultado.orcamento()).isNull();
    }

    @Test
    void deveConverterParaDtoComOrcamento() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo();
        Servico servico = criarServico();
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .cliente(cliente)
                .veiculo(veiculo)
                .relatoCliente(RELATO)
                .status(Status.RECEBIDA)
                .build();

        Orcamento orcamento = criarOrcamento();
        OrdemServicoServico ordemServicoServico = OrdemServicoServico.builder()
                .orcamento(orcamento)
                .servico(servico)
                .quantidade(1)
                .build();
        orcamento.setServicos(List.of(ordemServicoServico));
        orcamento.recalcularPrecoTotal();
        ordemServico.setOrcamento(orcamento);

        OrdemServicoDto resultado = OrdemServicoMapper.toDto(ordemServico);

        assertThat(resultado.orcamento()).isNotNull();
        assertThat(resultado.orcamento().servicos()).hasSize(1);
        assertThat(resultado.orcamento().precoTotal()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(resultado.relatoCliente()).isEqualTo(RELATO);
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID_CLIENTE)
                .nome("João Silva")
                .documento("12345678901")
                .email("joao@email.com")
                .telefone("11999999999")
                .build();
    }

    private Veiculo criarVeiculo() {
        return Veiculo.builder()
                .id(ID_VEICULO)
                .marca("Fiat")
                .modelo("Uno")
                .placa("ABC1234")
                .ano(2020)
                .ativo(true)
                .build();
    }

    private Servico criarServico() {
        return Servico.builder()
                .id(ID_SERVICO)
                .nome("Troca de óleo")
                .descricao("Troca completa do óleo do motor")
                .valor(new BigDecimal("150.00"))
                .ativo(true)
                .build();
    }

    private Insumo criarInsumo() {
        return Insumo.builder()
                .id(ID_INSUMO)
                .nome("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .build();
    }

    private Orcamento criarOrcamento() {
        return Orcamento.builder()
                .build();
    }
}
