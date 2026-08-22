package com.fiap.mecanica.atendimento.controller;

import com.fiap.mecanica.atendimento.controller.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.atendimento.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.dto.OrdemServicoDto;
import com.fiap.mecanica.atendimento.dto.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.atendimento.service.OrdemServicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private OrdemServicoService service;

    @InjectMocks
    private AtendimentoController controller;

    @Test
    void deveIniciarAtendimentoComSucesso() {
        IniciarAtendimentoRequest request = new IniciarAtendimentoRequest(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.RECEBIDA)
                .build();

        when(service.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null)).thenReturn(dto);

        OrdemServicoDto resultado = controller.iniciarAtendimento(request);

        assertThat(resultado).isEqualTo(dto);
        verify(service).iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
    }

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .build();

        when(service.buscarPorId(UUID_ORDEM)).thenReturn(dto);

        OrdemServicoDto resultado = controller.buscarPorId(UUID_ORDEM);

        assertThat(resultado).isEqualTo(dto);
        verify(service).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveListarAtendimentosEmAbertoComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder().id(UUID_ORDEM).build();
        when(service.listarAtendimentosEmAberto()).thenReturn(List.of(dto));

        List<OrdemServicoDto> resultado = controller.listarAbertos();

        assertThat(resultado).containsExactly(dto);
        verify(service).listarAtendimentosEmAberto();
    }

    @Test
    void deveListarTempoMedioExecucaoServicosComSucesso() {
        TempoMedioExecucaoServicoDto dto = TempoMedioExecucaoServicoDto.builder()
                .servicoId(1L)
                .nome("Troca de oleo")
                .ordensFinalizadas(2L)
                .tempoMedioExecucaoMinutos(80L)
                .build();
        when(service.listarTempoMedioExecucaoServicos()).thenReturn(List.of(dto));

        List<TempoMedioExecucaoServicoDto> resultado = controller.listarTempoMedioExecucaoServicos();

        assertThat(resultado).containsExactly(dto);
        verify(service).listarTempoMedioExecucaoServicos();
    }

    @Test
    void deveIniciarDiagnosticoComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.EM_DIAGNOSTICO)
                .build();

        when(service.iniciarDiagnostico(UUID_ORDEM)).thenReturn(dto);

        OrdemServicoDto resultado = controller.iniciarDiagnostico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(dto);
        verify(service).iniciarDiagnostico(UUID_ORDEM);
    }

    @Test
    void deveFinalizarDiagnosticoAoOrcamentoComSucesso() {
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicos = List.of(new IniciarAtendimentoRequest.ServicoQuantidade(1L, 1));
        AdicionarItensOrcamentoRequest request = new AdicionarItensOrcamentoRequest(servicos, null, "Obs");
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .build();

        when(service.realizarDiagnostico(UUID_ORDEM, servicos, null, "Obs")).thenReturn(dto);

        OrdemServicoDto resultado = controller.finalizarDiagnostico(UUID_ORDEM, request);

        assertThat(resultado).isEqualTo(dto);
        verify(service).realizarDiagnostico(UUID_ORDEM, servicos, null, "Obs");
    }

    @Test
    void deveAprovarOrdemServicoComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.EM_EXECUCAO)
                .build();

        when(service.aprovarOrdemServico(UUID_ORDEM)).thenReturn(dto);

        OrdemServicoDto resultado = controller.aprovarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(dto);
        verify(service).aprovarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveFinalizarOrdemServicoComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.FINALIZADA)
                .build();

        when(service.finalizarOrdemServico(UUID_ORDEM)).thenReturn(dto);

        OrdemServicoDto resultado = controller.finalizarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(dto);
        verify(service).finalizarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveCancelarOrdemServicoComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.CANCELADA)
                .build();

        when(service.cancelarOrdemServico(UUID_ORDEM)).thenReturn(dto);

        OrdemServicoDto resultado = controller.cancelarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(dto);
        verify(service).cancelarOrdemServico(UUID_ORDEM);
    }

    @Test
    void deveEntregarOrdemServicoComSucesso() {
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.ENTREGUE)
                .build();

        when(service.entregarVeiculo(UUID_ORDEM)).thenReturn(dto);

        OrdemServicoDto resultado = controller.entregarOrdemServico(UUID_ORDEM);

        assertThat(resultado).isEqualTo(dto);
        verify(service).entregarVeiculo(UUID_ORDEM);
    }
}
