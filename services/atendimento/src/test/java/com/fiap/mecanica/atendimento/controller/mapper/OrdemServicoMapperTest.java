package com.fiap.mecanica.atendimento.controller.mapper;

import com.fiap.mecanica.atendimento.domain.Orcamento;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoInsumo;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.dto.OrdemServicoDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        OrdemServico resultado = OrdemServicoMapper.toEntity(ID_CLIENTE, ID_VEICULO, "Problema no motor");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getClienteId()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(resultado.getRelatoCliente()).isEqualTo("Problema no motor");
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.getId()).isNull();
    }

    // ===================== toDto =====================

    @Test
    void deveConverterParaDtoSemOrcamento() {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .relatoCliente(RELATO)
                .status(Status.RECEBIDA)
                .build();

        OrdemServicoDto resultado = OrdemServicoMapper.toDto(ordemServico);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.RECEBIDA);
        assertThat(resultado.descricaoStatus()).isEqualTo(Status.RECEBIDA.getDescricao());
        assertThat(resultado.clienteId()).isEqualTo(ID_CLIENTE);
        assertThat(resultado.veiculoId()).isEqualTo(ID_VEICULO);
        assertThat(resultado.relatoCliente()).isEqualTo(RELATO);
        assertThat(resultado.orcamento()).isNull();
    }

    @Test
    void deveConverterParaDtoComOrcamento() {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .relatoCliente(RELATO)
                .status(Status.RECEBIDA)
                .build();

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .servicos(new ArrayList<>())
                .insumos(new ArrayList<>())
                .build();

        OrdemServicoServico ordemServicoServico = OrdemServicoServico.builder()
                .orcamento(orcamento)
                .servicoId(ID_SERVICO)
                .nomeServico("Troca de óleo")
                .valorUnitario(new BigDecimal("150.00"))
                .quantidade(1)
                .build();
        OrdemServicoInsumo ordemServicoInsumo = OrdemServicoInsumo.builder()
                .orcamento(orcamento)
                .insumoId(ID_INSUMO)
                .nomeInsumo("Óleo de motor")
                .precoUnitario(new BigDecimal("45.90"))
                .quantidade(2)
                .build();

        orcamento.setServicos(new ArrayList<>(List.of(ordemServicoServico)));
        orcamento.setInsumos(new ArrayList<>(List.of(ordemServicoInsumo)));
        orcamento.recalcularPrecoTotal();
        ordemServico.setOrcamento(orcamento);

        OrdemServicoDto resultado = OrdemServicoMapper.toDto(ordemServico);

        assertThat(resultado.orcamento()).isNotNull();
        assertThat(resultado.orcamento().servicos()).hasSize(1);
        assertThat(resultado.orcamento().servicos().getFirst().servicoId()).isEqualTo(ID_SERVICO);
        assertThat(resultado.orcamento().servicos().getFirst().nomeServico()).isEqualTo("Troca de óleo");
        assertThat(resultado.orcamento().insumos()).hasSize(1);
        assertThat(resultado.orcamento().insumos().getFirst().insumoId()).isEqualTo(ID_INSUMO);
        assertThat(resultado.orcamento().precoTotal()).isEqualByComparingTo(new BigDecimal("241.80"));
        assertThat(resultado.relatoCliente()).isEqualTo(RELATO);
    }

    @Test
    void deveConverterParaDtoComStatusNulo() {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(UUID_ORDEM)
                .clienteId(ID_CLIENTE)
                .veiculoId(ID_VEICULO)
                .status(null)
                .build();

        OrdemServicoDto resultado = OrdemServicoMapper.toDto(ordemServico);

        assertThat(resultado.status()).isNull();
        assertThat(resultado.descricaoStatus()).isNull();
    }
}
