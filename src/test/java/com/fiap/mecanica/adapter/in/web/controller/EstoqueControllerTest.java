package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.request.AtualizarEstoqueRequest;
import com.fiap.mecanica.adapter.in.web.request.AtualizarInsumoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarInsumoRequest;
import com.fiap.mecanica.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;
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
class EstoqueControllerTest {
    @Mock
    private EstoqueUseCase service;

    @InjectMocks
    private EstoqueController controller;

    @Test
    void deveListarInsumosAtivosDoEstoque() {
        when(service.listarEstoque()).thenReturn(List.of(criarEstoque(true)));

        assertThat(controller.list())
                .singleElement()
                .satisfies(estoque -> {
                    assertThat(estoque.insumo().id()).isEqualTo(1L);
                    assertThat(estoque.insumo().nome()).isEqualTo("Oleo");
                    assertThat(estoque.quantidadeInsumo()).isEqualTo(10L);
                });
    }

    @Test
    void deveExecutarOperacoesDoEstoquePeloIdDoInsumo() {
        Estoque ativo = criarEstoque(true);
        Estoque inativo = criarEstoque(false);
        when(service.buscarPorIdInsumo(1L)).thenReturn(ativo);
        when(service.cadastrarEstoque(any())).thenReturn(ativo);
        when(service.atualizarQuantidade(1L, 20L)).thenReturn(ativo);
        when(service.atualizarInsumo(eq(1L), any())).thenReturn(ativo);
        when(service.excluirEstoque(1L)).thenReturn(inativo);
        when(service.reativarEstoque(1L)).thenReturn(ativo);

        assertThat(controller.get(1L).insumo().nome()).isEqualTo("Oleo");
        assertThat(controller.create(criarRequest()).insumo().id()).isEqualTo(1L);
        assertThat(controller.updateQuantidade(1L, new AtualizarEstoqueRequest(20L)).insumo().id()).isEqualTo(1L);
        assertThat(controller.updateInsumo(1L, new AtualizarInsumoRequest("Filtro", null)).insumo().id()).isEqualTo(1L);
        assertThat(controller.delete(1L).insumo().id()).isEqualTo(1L);
        assertThat(controller.reativar(1L).insumo().id()).isEqualTo(1L);

        ArgumentCaptor<Estoque> captor = ArgumentCaptor.forClass(Estoque.class);
        verify(service).cadastrarEstoque(captor.capture());
        assertThat(captor.getValue().getInsumo().getNome()).isEqualTo("Oleo");
        assertThat(captor.getValue().getQuantidadeInsumo()).isEqualTo(10L);

        ArgumentCaptor<AtualizarInsumoCommand> commandCaptor =
                ArgumentCaptor.forClass(AtualizarInsumoCommand.class);
        verify(service).atualizarInsumo(eq(1L), commandCaptor.capture());
        assertThat(commandCaptor.getValue().nome()).isEqualTo("Filtro");
    }

    private CadastrarEstoqueRequest criarRequest() {
        return new CadastrarEstoqueRequest(
                new CadastrarInsumoRequest("Oleo", new BigDecimal("45.90")), 10L);
    }

    private Estoque criarEstoque(boolean ativo) {
        return Estoque.builder()
                .id(10L)
                .insumo(Insumo.builder()
                        .id(1L)
                        .nome("Oleo")
                        .precoUnitario(new BigDecimal("45.90"))
                        .build())
                .quantidadeInsumo(10L)
                .ativo(ativo)
                .build();
    }
}
