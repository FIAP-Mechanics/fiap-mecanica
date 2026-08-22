package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.request.AtualizarServicoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarServicoRequest;
import com.fiap.mecanica.adapter.in.web.response.ServicoDto;
import com.fiap.mecanica.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.application.port.in.ServicoUseCase;
import com.fiap.mecanica.domain.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoUseCase service;

    @InjectMocks
    private ServicoController controller;

    @Test
    void deveRetornarServicoPorId() {
        when(service.buscarServicoPorId(1L)).thenReturn(criarServico(true));

        ServicoDto resultado = controller.get(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Troca dos pneus dianteiros");
    }

    @Test
    void deveCadastrarServicoIndependente() {
        CadastrarServicoRequest request = new CadastrarServicoRequest(
                "Troca dos pneus dianteiros", "Substituicao", new BigDecimal("120.00"));
        when(service.cadastrarServico(any())).thenReturn(criarServico(true));

        controller.create(request);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(service).cadastrarServico(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Troca dos pneus dianteiros");
        assertThat(captor.getValue().getValor()).isEqualByComparingTo("120.00");
    }

    @Test
    void deveAtualizarServico() {
        AtualizarServicoRequest request = new AtualizarServicoRequest("Novo nome", null, null);
        when(service.atualizarServico(eq(1L), any())).thenReturn(criarServico(true));

        controller.update(1L, request);

        ArgumentCaptor<AtualizarServicoCommand> captor = ArgumentCaptor.forClass(AtualizarServicoCommand.class);
        verify(service).atualizarServico(eq(1L), captor.capture());
        assertThat(captor.getValue().nome()).isEqualTo("Novo nome");
    }

    @Test
    void deveExcluirServicoLogicamente() {
        when(service.excluirServico(1L)).thenReturn(criarServico(false));

        assertThat(controller.delete(1L).id()).isEqualTo(1L);
        verify(service).excluirServico(1L);
    }

    @Test
    void deveReativarServico() {
        when(service.reativarServico(1L)).thenReturn(criarServico(true));

        assertThat(controller.reativar(1L).id()).isEqualTo(1L);
        verify(service).reativarServico(1L);
    }

    private Servico criarServico(boolean ativo) {
        return Servico.builder()
                .id(1L)
                .nome("Troca dos pneus dianteiros")
                .descricao("Substituicao")
                .valor(new BigDecimal("120.00"))
                .ativo(ativo)
                .build();
    }
}
