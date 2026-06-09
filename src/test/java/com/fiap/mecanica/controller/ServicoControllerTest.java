package com.fiap.mecanica.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoService service;

    @InjectMocks
    private ServicoController controller;

    @Test
    void deveRetornarServicoPorId() {
        when(service.buscarServicoPorId(1L)).thenReturn(criarServico());

        ServicoDto resultado = controller.get(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Troca dos pneus dianteiros");
        assertThat(resultado.insumos()).hasSize(1);
    }

    @Test
    void deveCadastrarServicoComQuantidadeDoInsumo() {
        ServicoInsumo item = criarItem();
        CadastrarServicoRequest request = new CadastrarServicoRequest(
                "Troca dos pneus dianteiros", "Substituicao", new BigDecimal("120.00"), List.of(item));
        when(service.cadastrarServico(any())).thenReturn(criarServico());

        controller.create(request);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(service).cadastrarServico(captor.capture());
        assertThat(captor.getValue().getInsumos()).hasSize(1);
        assertThat(captor.getValue().getInsumos().getFirst().getQuantidadeUtilizada()).isEqualByComparingTo("2");
        assertThat(captor.getValue().getInsumos().getFirst().getServico()).isSameAs(captor.getValue());
    }

    @Test
    void deveCadastrarServicoSemInsumos() {
        CadastrarServicoRequest request = new CadastrarServicoRequest(
                "Diagnostico", "Avaliacao", new BigDecimal("80.00"), null);
        when(service.cadastrarServico(any())).thenReturn(criarServico());

        controller.create(request);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(service).cadastrarServico(captor.capture());
        assertThat(captor.getValue().getInsumos()).isEmpty();
    }

    @Test
    void deveAtualizarServico() {
        AtualizarServicoRequest request = new AtualizarServicoRequest(
                "Novo nome", null, null, List.of());
        when(service.atualizarServico(eq(1L), any())).thenReturn(criarServico());

        controller.update(1L, request);

        ArgumentCaptor<ServicoDto> captor = ArgumentCaptor.forClass(ServicoDto.class);
        verify(service).atualizarServico(eq(1L), captor.capture());
        assertThat(captor.getValue().nome()).isEqualTo("Novo nome");
        assertThat(captor.getValue().insumos()).isEmpty();
    }

    @Test
    void deveExcluirServicoLogicamente() {
        Servico servico = criarServico();
        servico.setAtivo(false);
        when(service.excluirServico(1L)).thenReturn(servico);

        assertThat(controller.delete(1L).id()).isEqualTo(1L);
        verify(service).excluirServico(1L);
    }

    private Servico criarServico() {
        Servico servico = Servico.builder()
                .id(1L)
                .nome("Troca dos pneus dianteiros")
                .descricao("Substituicao")
                .valor(new BigDecimal("120.00"))
                .build();
        servico.atualizarInsumos(List.of(criarItem()));
        return servico;
    }

    private ServicoInsumo criarItem() {
        return ServicoInsumo.builder()
                .insumo(Insumo.builder().id(1L).nome("Pneu").preco(new BigDecimal("450.00")).build())
                .quantidadeUtilizada(new BigDecimal("2"))
                .build();
    }
}
