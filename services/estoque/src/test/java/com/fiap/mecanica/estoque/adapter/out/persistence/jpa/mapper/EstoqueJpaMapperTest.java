package com.fiap.mecanica.estoque.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.EstoqueJpaEntity;
import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.InsumoJpaEntity;
import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EstoqueJpaMapperTest {

    private static final Long ID = 10L;
    private static final Long ID_INSUMO = 1L;
    private static final String NOME = "Oleo";
    private static final BigDecimal PRECO = new BigDecimal("45.90");
    private static final Long QUANTIDADE = 10L;

    @Test
    void deveConverterEntityParaDomain() {
        EstoqueJpaEntity entity = criarEntity();

        Estoque estoque = EstoqueJpaMapper.toDomain(entity);

        assertThat(estoque.getId()).isEqualTo(ID);
        assertThat(estoque.getInsumo().getId()).isEqualTo(ID_INSUMO);
        assertThat(estoque.getInsumo().getNome()).isEqualTo(NOME);
        assertThat(estoque.getQuantidadeInsumo()).isEqualTo(QUANTIDADE);
        assertThat(estoque.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(EstoqueJpaMapper.toDomain(null)).isNull();
    }

    @Test
    void deveConverterDomainParaEntity() {
        Estoque estoque = criarEstoque();

        EstoqueJpaEntity entity = EstoqueJpaMapper.toJpaEntity(estoque);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getInsumo().getId()).isEqualTo(ID_INSUMO);
        assertThat(entity.getInsumo().getNome()).isEqualTo(NOME);
        assertThat(entity.getQuantidadeInsumo()).isEqualTo(QUANTIDADE);
        assertThat(entity.isAtivo()).isTrue();
    }

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(EstoqueJpaMapper.toJpaEntity(null)).isNull();
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
