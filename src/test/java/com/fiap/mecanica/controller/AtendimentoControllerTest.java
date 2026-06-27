package com.fiap.mecanica.controller;

import com.fiap.mecanica.controller.request.AdicionarItensOrcamentoRequest;
import com.fiap.mecanica.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.domain.Status;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.service.OrdemServicoService;
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
        // Arrange
        IniciarAtendimentoRequest request = new IniciarAtendimentoRequest(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.RECEBIDA)
                .build();

        when(service.iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null)).thenReturn(dto);

        // Act
        OrdemServicoDto resultado = controller.iniciarAtendimento(request);

        // Assert
        assertThat(resultado).isEqualTo(dto);
        verify(service).iniciarAtendimento(ID_CLIENTE, ID_VEICULO, RELATO, null, null);
    }

    @Test
    void deveBuscarOrdemServicoPorIdComSucesso() {
        // Arrange
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .build();

        when(service.buscarPorId(UUID_ORDEM)).thenReturn(dto);

        // Act
        OrdemServicoDto resultado = controller.buscarPorId(UUID_ORDEM);

        // Assert
        assertThat(resultado).isEqualTo(dto);
        verify(service).buscarPorId(UUID_ORDEM);
    }

    @Test
    void deveListarAtendimentosEmAbertoComSucesso() {
        // Arrange
        OrdemServicoDto dto = OrdemServicoDto.builder().id(UUID_ORDEM).build();
        when(service.listarAtendimentosEmAberto()).thenReturn(List.of(dto));

        // Act
        List<OrdemServicoDto> resultado = controller.listarAbertos();

        // Assert
        assertThat(resultado).containsExactly(dto);
        verify(service).listarAtendimentosEmAberto();
    }

    @Test
    void deveIniciarDiagnosticoComSucesso() {
        // Arrange
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .status(Status.EM_DIAGNOSTICO)
                .build();

        when(service.iniciarDiagnostico(UUID_ORDEM)).thenReturn(dto);

        // Act
        OrdemServicoDto resultado = controller.iniciarDiagnostico(UUID_ORDEM);

        // Assert
        assertThat(resultado).isEqualTo(dto);
        verify(service).iniciarDiagnostico(UUID_ORDEM);
    }

    @Test
    void deveAdicionarItensAoOrcamentoComSucesso() {
        // Arrange
        List<IniciarAtendimentoRequest.ServicoQuantidade> servicos = List.of(new IniciarAtendimentoRequest.ServicoQuantidade(1L, 1));
        AdicionarItensOrcamentoRequest request = new AdicionarItensOrcamentoRequest(servicos, null, "Obs");
        OrdemServicoDto dto = OrdemServicoDto.builder()
                .id(UUID_ORDEM)
                .build();

        when(service.adicionarItens(UUID_ORDEM, servicos, null, "Obs")).thenReturn(dto);

        // Act
        OrdemServicoDto resultado = controller.adicionarItens(UUID_ORDEM, request);

        // Assert
        assertThat(resultado).isEqualTo(dto);
        verify(service).adicionarItens(UUID_ORDEM, servicos, null, "Obs");
    }
}
