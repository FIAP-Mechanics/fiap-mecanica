package com.fiap.mecanica.estoque.service;

import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;
import com.fiap.mecanica.estoque.dto.DeduzirEstoqueItemDto;
import com.fiap.mecanica.estoque.dto.InsumoDto;
import com.fiap.mecanica.estoque.exception.EstoqueInativoException;
import com.fiap.mecanica.estoque.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.estoque.exception.EstoqueNotFound;
import com.fiap.mecanica.estoque.repository.EstoqueRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository repository;

    @InjectMocks
    private EstoqueService service;

    @Test
    void deveCadastrarEstoque() {
        Estoque estoque = new Estoque();
        when(repository.save(any(Estoque.class))).thenReturn(estoque);

        Estoque result = service.cadastrarEstoque(estoque);

        assertThat(result).isEqualTo(estoque);
        verify(repository).save(estoque);
    }

    @Test
    void deveBuscarPorIdInsumo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));

        Estoque result = service.buscarPorIdInsumo(1L);

        assertThat(result).isEqualTo(estoque);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarInsumoInativo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, false);
        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> service.buscarPorIdInsumo(1L))
                .isInstanceOf(EstoqueInativoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueNaoEncontrado() {
        when(repository.findByInsumoId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorIdInsumo(1L))
                .isInstanceOf(EstoqueNotFound.class);
    }

    @Test
    void deveListarEstoqueAtivo() {
        Estoque estoque = new Estoque();
        when(repository.findAllByAtivoTrue()).thenReturn(List.of(estoque));

        List<Estoque> result = service.listarEstoque();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(estoque);
    }

    @Test
    void deveAtualizarQuantidade() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.save(any(Estoque.class))).thenReturn(estoque);

        Estoque result = service.atualizarQuantidade(1L, 20L);

        assertThat(result.getQuantidadeInsumo()).isEqualTo(20L);
        verify(repository).save(estoque);
    }

    @Test
    void deveAtualizarInsumo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        InsumoDto dto = new InsumoDto(1L, "Oleo Novo", new BigDecimal("60.00"));

        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.save(any(Estoque.class))).thenReturn(estoque);

        Estoque result = service.atualizarInsumo(1L, dto);

        assertThat(result.getInsumo().getNome()).isEqualTo("Oleo Novo");
        assertThat(result.getInsumo().getPrecoUnitario()).isEqualTo(new BigDecimal("60.00"));
        verify(repository).save(estoque);
    }

    @Test
    void deveExcluirEstoqueLogicamente() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.save(any(Estoque.class))).thenReturn(estoque);

        Estoque result = service.excluirEstoque(1L);

        assertThat(result.isAtivo()).isFalse();
        verify(repository).save(estoque);
    }

    @Test
    void deveDeduzirEstoque() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        DeduzirEstoqueItemDto item = new DeduzirEstoqueItemDto(1L, 5L);

        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));

        service.deduzirEstoque(List.of(item));

        assertThat(estoque.getQuantidadeInsumo()).isEqualTo(5L);
        verify(repository).save(estoque);
    }

    @Test
    void deveLancarExcecaoSeEstoqueInsuficienteAoDeduzir() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 3L, true);
        DeduzirEstoqueItemDto item = new DeduzirEstoqueItemDto(1L, 5L);

        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> service.deduzirEstoque(List.of(item)))
                .isInstanceOf(EstoqueInsuficienteException.class);
    }

    @Test
    void deveReativarEstoque() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, false);
        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.save(any(Estoque.class))).thenReturn(estoque);

        Estoque result = service.reativarEstoque(1L);

        assertThat(result.isAtivo()).isTrue();
        verify(repository).save(estoque);
    }

    @Test
    void deveLancarExcecaoAoReativarEstoqueJaAtivo() {
        Insumo insumo = new Insumo(1L, "Oleo", new BigDecimal("50.00"));
        Estoque estoque = new Estoque(1L, insumo, 10L, true);
        when(repository.findByInsumoId(1L)).thenReturn(Optional.of(estoque));

        assertThatThrownBy(() -> service.reativarEstoque(1L))
                .isInstanceOf(EstoqueJaAtivoException.class);
    }
}
