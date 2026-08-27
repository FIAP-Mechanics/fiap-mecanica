package com.fiap.mecanica.estoque.application.usecase;

import com.fiap.mecanica.estoque.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.estoque.application.command.DeduzirEstoqueItemCommand;
import com.fiap.mecanica.estoque.application.port.out.EstoqueGateway;
import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;
import com.fiap.mecanica.estoque.exception.EstoqueInativoException;
import com.fiap.mecanica.estoque.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.estoque.exception.EstoqueNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueInteractorTest {

    @Mock
    private EstoqueGateway estoqueGateway;

    @InjectMocks
    private EstoqueInteractor interactor;

    @Test
    void deveCadastrarEstoque() {
        Estoque estoque = new Estoque();
        when(estoqueGateway.salvar(any(Estoque.class))).thenReturn(estoque);

        Estoque result = interactor.cadastrarEstoque(estoque);

        assertThat(result).isEqualTo(estoque);
        verify(estoqueGateway).salvar(estoque);
    }

    @Test
    void deveBuscarPorIdInsumo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));

        Estoque result = interactor.buscarPorIdInsumo(1L);

        assertThat(result).isEqualTo(estoque);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarInsumoInativo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, false);
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> interactor.buscarPorIdInsumo(1L))
                .isInstanceOf(EstoqueInativoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueNaoEncontrado() {
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarPorIdInsumo(1L))
                .isInstanceOf(EstoqueNotFound.class);
    }

    @Test
    void deveListarEstoqueAtivo() {
        Estoque estoque = new Estoque();
        when(estoqueGateway.buscarTodosAtivos()).thenReturn(List.of(estoque));

        List<Estoque> result = interactor.listarEstoque();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(estoque);
    }

    @Test
    void deveAtualizarQuantidade() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));
        when(estoqueGateway.salvar(any(Estoque.class))).thenReturn(estoque);

        Estoque result = interactor.atualizarQuantidade(1L, 20L);

        assertThat(result.getQuantidadeInsumo()).isEqualTo(20L);
        verify(estoqueGateway).salvar(estoque);
    }

    @Test
    void deveAtualizarInsumo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        AtualizarInsumoCommand command = AtualizarInsumoCommand.builder()
                .nome("Oleo Novo")
                .precoUnitario(new BigDecimal("60.00"))
                .build();

        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));
        when(estoqueGateway.salvar(any(Estoque.class))).thenReturn(estoque);

        Estoque result = interactor.atualizarInsumo(1L, command);

        assertThat(result.getInsumo().getNome()).isEqualTo("Oleo Novo");
        assertThat(result.getInsumo().getPrecoUnitario()).isEqualTo(new BigDecimal("60.00"));
        verify(estoqueGateway).salvar(estoque);
    }

    @Test
    void deveManterCamposDoInsumoQuandoCommandForParcial() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        AtualizarInsumoCommand command = AtualizarInsumoCommand.builder().build();

        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));
        when(estoqueGateway.salvar(any(Estoque.class))).thenReturn(estoque);

        Estoque result = interactor.atualizarInsumo(1L, command);

        assertThat(result.getInsumo().getNome()).isEqualTo("Oleo");
        assertThat(result.getInsumo().getPrecoUnitario()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void deveExcluirEstoqueLogicamente() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));
        when(estoqueGateway.salvar(any(Estoque.class))).thenReturn(estoque);

        Estoque result = interactor.excluirEstoque(1L);

        assertThat(result.isAtivo()).isFalse();
        verify(estoqueGateway).salvar(estoque);
    }

    @Test
    void deveDeduzirEstoque() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        DeduzirEstoqueItemCommand item = DeduzirEstoqueItemCommand.builder().insumoId(1L).quantidade(5L).build();

        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));

        interactor.deduzirEstoque(List.of(item));

        assertThat(estoque.getQuantidadeInsumo()).isEqualTo(5L);
        verify(estoqueGateway).salvarTodos(List.of(estoque));
    }

    @Test
    void deveLancarExcecaoSeEstoqueInsuficienteAoDeduzir() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 3L, true);
        DeduzirEstoqueItemCommand item = DeduzirEstoqueItemCommand.builder().insumoId(1L).quantidade(5L).build();

        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> interactor.deduzirEstoque(List.of(item)))
                .isInstanceOf(EstoqueInsuficienteException.class);

        verify(estoqueGateway, never()).salvarTodos(any());
    }

    @Test
    void deveReativarEstoque() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, false);
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));
        when(estoqueGateway.salvar(any(Estoque.class))).thenReturn(estoque);

        Estoque result = interactor.reativarEstoque(1L);

        assertThat(result.isAtivo()).isTrue();
        verify(estoqueGateway).salvar(estoque);
    }

    @Test
    void deveLancarExcecaoAoReativarEstoqueJaAtivo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(estoqueGateway.buscarPorIdInsumo(1L)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> interactor.reativarEstoque(1L))
                .isInstanceOf(EstoqueJaAtivoException.class);
    }
}
