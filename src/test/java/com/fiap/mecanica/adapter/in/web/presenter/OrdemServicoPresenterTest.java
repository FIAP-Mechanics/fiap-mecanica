package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.adapter.in.web.request.FinalizarOrdemServicoRequest;
import com.fiap.mecanica.adapter.in.web.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.adapter.in.web.response.OrcamentoDto;
import com.fiap.mecanica.adapter.in.web.response.OrcamentoInsumoDto;
import com.fiap.mecanica.adapter.in.web.response.OrdemServicoDto;
import com.fiap.mecanica.adapter.in.web.response.OrdemServicoServicoDto;
import com.fiap.mecanica.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoTempoCommand;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.Orcamento;
import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.OrdemServicoInsumo;
import com.fiap.mecanica.domain.OrdemServicoServico;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.domain.Status;
import com.fiap.mecanica.domain.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoPresenterTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final Long ID_SERVICO = 3L;
    private static final Long ID_INSUMO = 4L;
    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final String RELATO = "Problema relatado pelo cliente";

    @Test
    void deveConverterItensDoAtendimentoParaCommands() {
        IniciarAtendimentoRequest request = new IniciarAtendimentoRequest(
                ID_CLIENTE,
                ID_VEICULO,
                "Problema no motor",
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 2)),
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 3)));

        List<ServicoQuantidadeCommand> servicos = OrdemServicoPresenter.toServicoCommands(request);
        List<InsumoQuantidadeCommand> insumos = OrdemServicoPresenter.toInsumoCommands(request);

        assertThat(servicos).hasSize(1);
        assertThat(servicos.getFirst().servico()).isEqualTo(ID_SERVICO);
        assertThat(servicos.getFirst().quantidade()).isEqualTo(2);
        assertThat(insumos).hasSize(1);
        assertThat(insumos.getFirst().insumo()).isEqualTo(ID_INSUMO);
        assertThat(insumos.getFirst().quantidade()).isEqualTo(3);
    }

    @Test
    void deveConverterServicoAdicionadoAoOrcamentoParaCommand() {
        AdicionarItensOrcamentoRequest request = new AdicionarItensOrcamentoRequest(
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(ID_SERVICO, 2)),
                null,
                "Diagnóstico");

        List<ServicoQuantidadeCommand> resultado = OrdemServicoPresenter.toServicoCommands(request);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().servico()).isEqualTo(ID_SERVICO);
        assertThat(resultado.getFirst().quantidade()).isEqualTo(2);
    }

    @Test
    void deveConverterInsumoAdicionadoAoOrcamentoParaCommand() {
        AdicionarItensOrcamentoRequest request = new AdicionarItensOrcamentoRequest(
                null,
                List.of(new IniciarAtendimentoRequest.InsumoQuantidade(ID_INSUMO, 3)),
                "Diagnóstico");

        List<InsumoQuantidadeCommand> resultado = OrdemServicoPresenter.toInsumoCommands(request);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().insumo()).isEqualTo(ID_INSUMO);
        assertThat(resultado.getFirst().quantidade()).isEqualTo(3);
    }

    @Test
    void deveConverterTempoDeFinalizacaoParaCommand() {
        FinalizarOrdemServicoRequest request = new FinalizarOrdemServicoRequest(
                List.of(new FinalizarOrdemServicoRequest.ServicoTempo(ID_SERVICO, 90L)));

        List<ServicoTempoCommand> resultado = OrdemServicoPresenter.toServicoTempoCommands(request);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().servico()).isEqualTo(ID_SERVICO);
        assertThat(resultado.getFirst().tempoGastoMinutos()).isEqualTo(90L);
    }

    @Test
    void deveConverterOrdemServicoServicoParaDto() {
        Servico servico = criarServico();
        OrdemServicoServico ordemServicoServico = OrdemServicoServico.builder()
                .servico(servico)
                .quantidade(2)
                .tempoExecucaoMinutos(90L)
                .build();

        OrdemServicoServicoDto resultado = OrdemServicoPresenter.toServicoDto(ordemServicoServico);

        assertThat(resultado).isNotNull();
        assertThat(resultado.servicoId()).isEqualTo(ID_SERVICO);
        assertThat(resultado.nome()).isEqualTo("Troca de óleo");
        assertThat(resultado.valorUnitario()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(resultado.quantidade()).isEqualTo(2);
        assertThat(resultado.tempoExecucaoMinutos()).isEqualTo(90L);
        assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void deveConverterOrdemServicoInsumoParaDto() {
        Insumo insumo = criarInsumo();
        OrdemServicoInsumo ordemServicoInsumo = OrdemServicoInsumo.builder()
                .insumo(insumo)
                .quantidade(2)
                .build();

        OrcamentoInsumoDto resultado = OrdemServicoPresenter.toInsumoDto(ordemServicoInsumo);

        assertThat(resultado).isNotNull();
        assertThat(resultado.insumoId()).isEqualTo(ID_INSUMO);
        assertThat(resultado.nome()).isEqualTo("Óleo de motor");
        assertThat(resultado.precoUnitario()).isEqualByComparingTo(new BigDecimal("45.90"));
        assertThat(resultado.quantidade()).isEqualTo(2);
        assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("91.80"));
    }

    @Test
    void deveRetornarNullQuandoOrcamentoForNull() {
        OrcamentoDto resultado = OrdemServicoPresenter.toOrcamentoDto(null);

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

        OrcamentoDto resultado = OrdemServicoPresenter.toOrcamentoDto(orcamento);

        assertThat(resultado).isNotNull();
        assertThat(resultado.servicos()).hasSize(1);
        assertThat(resultado.servicos().getFirst().servicoId()).isEqualTo(ID_SERVICO);
        assertThat(resultado.insumos()).hasSize(1);
        assertThat(resultado.insumos().getFirst().insumoId()).isEqualTo(ID_INSUMO);
        assertThat(resultado.precoTotal()).isEqualByComparingTo(new BigDecimal("241.80"));
    }

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

        OrdemServicoDto resultado = OrdemServicoPresenter.toDto(ordemServico);

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

        OrdemServicoDto resultado = OrdemServicoPresenter.toDto(ordemServico);

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
        return Orcamento.builder().build();
    }
}
