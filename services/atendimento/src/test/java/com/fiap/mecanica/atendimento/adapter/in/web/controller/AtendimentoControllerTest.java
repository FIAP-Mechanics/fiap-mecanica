package com.fiap.mecanica.atendimento.adapter.in.web.controller;

import com.fiap.mecanica.atendimento.adapter.in.web.presenter.AtendimentoPresenter;
import com.fiap.mecanica.atendimento.adapter.in.web.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.atendimento.adapter.in.web.request.DecisaoOrcamentoRequest;
import com.fiap.mecanica.atendimento.adapter.in.web.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.atendimento.adapter.in.web.response.OrdemServicoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.StatusOrdemServicoDto;
import com.fiap.mecanica.atendimento.adapter.in.web.response.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.atendimento.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.atendimento.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.domain.TempoMedioExecucaoServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoControllerTest {

    private static final String UUID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 2L;
    private static final String RELATO = "Barulho na suspensão";

    @Mock
    private AtendimentoUseCase atendimentoUseCase;

    @InjectMocks
    private AtendimentoController controller;

    @Test
    void deveIniciarAtendimentoComSucesso() {
        IniciarAtendimentoRequest request = new IniciarAtendimentoRequest(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.RECEBIDA);

        when(atendimentoUseCase.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.iniciarAtendimento(request);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
    }

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, null);

        when(atendimentoUseCase.buscarPorId(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.buscarPorId(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveConsultarStatusComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.EM_DIAGNOSTICO);

        when(atendimentoUseCase.buscarPorId(UUID_ORDEM)).thenReturn(ordemServico);

        StatusOrdemServicoDto resultado = controller.consultarStatus(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toStatusDto(ordemServico));
        verify(atendimentoUseCase).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveRegistrarDecisaoExternaOrcamentoAprovadaComSucesso() {
        DecisaoOrcamentoRequest request = new DecisaoOrcamentoRequest(true);
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.EM_EXECUCAO);

        when(atendimentoUseCase.aprovarOrdemServico(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.registrarDecisaoExternaOrcamento(UUID_ORDEM, request);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).aprovarOrdemServico(UUID_ORDEM);
        verify(atendimentoUseCase, never()).cancelarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveRegistrarDecisaoExternaOrcamentoRecusadaComSucesso() {
        DecisaoOrcamentoRequest request = new DecisaoOrcamentoRequest(false);
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.CANCELADA);

        when(atendimentoUseCase.cancelarOrdemServico(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.registrarDecisaoExternaOrcamento(UUID_ORDEM, request);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).cancelarOrdemServico(UUID_ORDEM);
        verify(atendimentoUseCase, never()).aprovarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveListarAtendimentosEmAbertoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, null);
        when(atendimentoUseCase.listarAtendimentosEmAberto()).thenReturn(List.of(ordemServico));

        List<OrdemServicoDto> resultado = controller.listarAbertos();

        assertThat(resultado).containsExactly(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).listarAtendimentosEmAberto();
    }

    @Test
    void deveListarTempoMedioExecucaoServicosComSucesso() {
        TempoMedioExecucaoServico indicador = TempoMedioExecucaoServico.builder()
                .servicoId(1L)
                .nome("Troca de oleo")
                .ordensFinalizadas(2L)
                .tempoMedioExecucaoMinutos(80L)
                .build();
        when(atendimentoUseCase.listarTempoMedioExecucaoServicos()).thenReturn(List.of(indicador));

        List<TempoMedioExecucaoServicoDto> resultado = controller.listarTempoMedioExecucaoServicos();

        assertThat(resultado).containsExactly(AtendimentoPresenter.toDto(indicador));
        verify(atendimentoUseCase).listarTempoMedioExecucaoServicos();
    }

    @Test
    void deveIniciarDiagnosticoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.EM_DIAGNOSTICO);

        when(atendimentoUseCase.iniciarDiagnostico(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.iniciarDiagnostico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).iniciarDiagnostico(UUID_ORDEM);
    }

    @Test
    void deveFinalizarDiagnosticoAoOrcamentoComSucesso() {
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicos = List.of(new IniciarAtendimentoRequest.ServicoQuantidade(1L, 1));
        List<ServicoQuantidadeCommand> comandosServicos = List.of(new ServicoQuantidadeCommand(1L, 1));
        AdicionarItensOrcamentoRequest request = new AdicionarItensOrcamentoRequest(servicos, null, "Obs");
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, null);

        when(atendimentoUseCase.realizarDiagnostico(UUID_ORDEM, comandosServicos, null, "Obs")).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.finalizarDiagnostico(UUID_ORDEM, request);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).realizarDiagnostico(UUID_ORDEM, comandosServicos, null, "Obs");
    }

    @Test
    void deveAprovarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.EM_EXECUCAO);

        when(atendimentoUseCase.aprovarOrdemServico(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).aprovarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveFinalizarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.FINALIZADA);

        when(atendimentoUseCase.finalizarOrdemServico(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.finalizarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).finalizarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveCancelarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.CANCELADA);

        when(atendimentoUseCase.cancelarOrdemServico(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.cancelarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).cancelarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveEntregarOrdemServicoComSucesso() {
        OrdemServico ordemServico = criarOrdemServico(UUID_ORDEM, Status.ENTREGUE);

        when(atendimentoUseCase.entregarVeiculo(UUID_ORDEM)).thenReturn(ordemServico);

        OrdemServicoDto resultado = controller.entregarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(AtendimentoPresenter.toDto(ordemServico));
        verify(atendimentoUseCase).entregarVeiculo(UUID_ORDEM);
    }

    private OrdemServico criarOrdemServico(String id, Status status) {
        return OrdemServico.builder()
                .id(id)
                .status(status)
                .build();
    }
}
