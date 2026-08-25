package com.fiap.mecanica.estoque.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.EstoqueJpaEntity;
import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.InsumoJpaEntity;
import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.repository.EstoqueSpringDataRepository;
import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueJpaGatewayTest {

    private static final Long ID = 10L;
    private static final Long ID_INSUMO = 1L;
    private static final String NOME = "Oleo";
    private static final BigDecimal PRECO = new BigDecimal("45.90");
    private static final Long QUANTIDADE = 10L;

    @Mock
    private EstoqueSpringDataRepository repository;

    @InjectMocks
    private EstoqueJpaGateway gateway;

    @Test
    void deveRetornarTodosOsEstoquesAtivosConvertidosParaDomain() {
        when(repository.findAllByAtivoTrue()).thenReturn(List.of(criarEntity()));

        List<Estoque> resultado = gateway.buscarTodosAtivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverEstoqueAtivo() {
        when(repository.findAllByAtivoTrue()).thenReturn(List.of());

        assertThat(gateway.buscarTodosAtivos()).isEmpty();
    }

    @Test
    void deveRetornarEstoqueQuandoIdInsumoExistir() {
        when(repository.findByInsumoId(ID_INSUMO)).thenReturn(Optional.of(criarEntity()));

        Optional<Estoque> resultado = gateway.buscarPorIdInsumo(ID_INSUMO);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getInsumo().getId()).isEqualTo(ID_INSUMO);
    }

    @Test
    void deveRetornarOptionalVazioQuandoIdInsumoNaoExistir() {
        when(repository.findByInsumoId(ID_INSUMO)).thenReturn(Optional.empty());

        assertThat(gateway.buscarPorIdInsumo(ID_INSUMO)).isEmpty();
    }

    @Test
    void deveSalvarEstoqueERetornarDomainConvertido() {
        when(repository.save(any())).thenReturn(criarEntity());

        Estoque resultado = gateway.salvar(criarEstoque());

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getInsumo().getNome()).isEqualTo(NOME);
    }

    @Test
    void deveSalvarTodosOsEstoquesERetornarDomainConvertido() {
        when(repository.saveAll(any())).thenReturn(List.of(criarEntity()));

        List<Estoque> resultado = gateway.salvarTodos(List.of(criarEstoque()));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
    }

    private EstoqueJpaEntity criarEntity() {
        return EstoqueJpaEntity.builder()
                .id(ID)
                .insumo(InsumoJpaEntity.builder().id(ID_INSUMO).nome(NOME).precoUnitario(PRECO).build())
                .quantidadeInsumo(QUANTIDADE)
                .ativo(true)
                .build();
    }

    private Estoque criarEstoque() {
        return Estoque.builder()
                .id(ID)
                .insumo(Insumo.builder().id(ID_INSUMO).nome(NOME).precoUnitario(PRECO).build())
                .quantidadeInsumo(QUANTIDADE)
                .ativo(true)
                .build();
    }
}
