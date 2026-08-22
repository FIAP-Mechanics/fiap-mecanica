package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.adapter.in.web.request.FinalizarOrdemServicoRequest;
import com.fiap.mecanica.adapter.in.web.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.adapter.in.web.response.OrdemServicoDto;
import com.fiap.mecanica.adapter.in.web.response.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoTempoCommand;
import com.fiap.mecanica.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.application.result.TempoMedioExecucaoServicoResult;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.Status;
import com.fiap.mecanica.domain.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoControllerTest {

    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final String RELATO = "Barulho na suspensão";

    @Mock
    private AtendimentoUseCase service;

    @InjectMocks
    private AtendimentoController controller;

    @Test
    void deveIniciarAtendimentoComSucesso() {
        IniciarAtendimentoRequest request =
                new IniciarAtendimentoRequest(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
        when(service.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null))
                .thenReturn(criarOrdem(Status.RECEBIDA));

        OrdemServicoDto resultado = controller.iniciarAtendimento(request);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.RECEBIDA);
        verify(service).iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
    }

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        when(service.buscarPorId(UUID_ORDEM)).thenReturn(criarOrdem(Status.RECEBIDA));

        OrdemServicoDto resultado = controller.buscarPorId(UUID_ORDEM);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        verify(service).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveListarAtendimentosEmAbertoComSucesso() {
        when(service.listarAtendimentosEmAberto()).thenReturn(List.of(criarOrdem(Status.RECEBIDA)));

        List<OrdemServicoDto> resultado = controller.listarAbertos();

        assertThat(resultado)
                .singleElement()
                .satisfies(ordem -> assertThat(ordem.id()).isEqualTo(UUID_ORDEM));
        verify(service).listarAtendimentosEmAberto();
    }

    @Test
    void deveListarTempoMedioExecucaoServicosComSucesso() {
        TempoMedioExecucaoServicoResult result = new TempoMedioExecucaoServicoResult(
                1L, "Troca de oleo", 2L, new BigDecimal("80.00"));
        when(service.listarTempoMedioExecucaoServicos()).thenReturn(List.of(result));

        List<TempoMedioExecucaoServicoDto> resultado = controller.listarTempoMedioExecucaoServicos();

        assertThat(resultado)
                .singleElement()
                .satisfies(dto -> {
                    assertThat(dto.servicoId()).isEqualTo(1L);
                    assertThat(dto.nome()).isEqualTo("Troca de oleo");
                    assertThat(dto.ordensFinalizadas()).isEqualTo(2L);
                    assertThat(dto.tempoMedioExecucaoMinutos()).isEqualByComparingTo("80.00");
                });
        verify(service).listarTempoMedioExecucaoServicos();
    }

    @Test
    void deveIniciarDiagnosticoComSucesso() {
        when(service.iniciarDiagnostico(UUID_ORDEM)).thenReturn(criarOrdem(Status.EM_DIAGNOSTICO));

        OrdemServicoDto resultado = controller.iniciarDiagnostico(UUID_ORDEM);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.EM_DIAGNOSTICO);
        verify(service).iniciarDiagnostico(UUID_ORDEM);
    }

    @Test
    void deveFinalizarDiagnosticoAoOrcamentoComSucesso() {
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicos =
                List.of(new IniciarAtendimentoRequest.ServicoQuantidade(1L, 1));
        List<ServicoQuantidadeCommand> commands = List.of(new ServicoQuantidadeCommand(1L, 1));
        AdicionarItensOrcamentoRequest request = new AdicionarItensOrcamentoRequest(servicos, null, "Obs");
        when(service.realizarDiagnostico(UUID_ORDEM, commands, null, "Obs"))
                .thenReturn(criarOrdem(Status.AGUARDANDO_APROVACAO));

        OrdemServicoDto resultado = controller.finalizarDiagnostico(UUID_ORDEM, request);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.AGUARDANDO_APROVACAO);
        verify(service).realizarDiagnostico(UUID_ORDEM, commands, null, "Obs");
    }

    @Test
    void deveAprovarOrdemServicoComSucesso() {
        when(service.aprovarOrdemServico(UUID_ORDEM)).thenReturn(criarOrdem(Status.EM_EXECUCAO));

        OrdemServicoDto resultado = controller.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.EM_EXECUCAO);
        verify(service).aprovarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveFinalizarOrdemServicoComTemposDosServicosComSucesso() {
        List<FinalizarOrdemServicoRequest.ServicoTempo> servicosTempo =
                List.of(new FinalizarOrdemServicoRequest.ServicoTempo(1L, 90L));
        List<ServicoTempoCommand> commands = List.of(new ServicoTempoCommand(1L, 90L));
        FinalizarOrdemServicoRequest request = new FinalizarOrdemServicoRequest(servicosTempo);
        when(service.finalizarOrdemServico(UUID_ORDEM, commands)).thenReturn(criarOrdem(Status.FINALIZADA));

        OrdemServicoDto resultado = controller.finalizarOrdemServico(UUID_ORDEM, request);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.FINALIZADA);
        verify(service).finalizarOrdemServico(UUID_ORDEM, commands);
    }

    @Test
    void deveCancelarOrdemServicoComSucesso() {
        when(service.cancelarOrdemServico(UUID_ORDEM)).thenReturn(criarOrdem(Status.CANCELADA));

        OrdemServicoDto resultado = controller.cancelarOrdemServico(UUID_ORDEM);

        assertThat(resultado.id()).isEqualTo(UUID_ORDEM);
        assertThat(resultado.status()).isEqualTo(Status.CANCELADA);
        verify(service).cancelarOrdemServico(UUID_ORDEM);
    }

    private OrdemServico criarOrdem(Status status) {
        return OrdemServico.builder()
                .id(UUID_ORDEM)
                .status(status)
                .cliente(Cliente.builder().id(ID_CLIENTE).build())
                .veiculo(Veiculo.builder().id(ID_VEICULO).build())
                .relatoCliente(RELATO)
                .historicoDeEventos(List.of())
                .build();
    }
}
