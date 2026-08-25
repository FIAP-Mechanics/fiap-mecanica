package com.fiap.mecanica.estoque.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.InsumoJpaEntity;
import com.fiap.mecanica.estoque.domain.Insumo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InsumoJpaMapperTest {

    private static final Long ID = 1L;
    private static final String NOME = "Oleo";
    private static final BigDecimal PRECO = new BigDecimal("45.90");

    @Test
    void deveConverterEntityParaDomain() {
        InsumoJpaEntity entity = InsumoJpaEntity.builder().id(ID).nome(NOME).precoUnitario(PRECO).build();

        Insumo insumo = InsumoJpaMapper.toDomain(entity);

        assertThat(insumo.getId()).isEqualTo(ID);
        assertThat(insumo.getNome()).isEqualTo(NOME);
        assertThat(insumo.getPrecoUnitario()).isEqualTo(PRECO);
    }

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(InsumoJpaMapper.toDomain(null)).isNull();
    }

    @Test
    void deveConverterDomainParaEntity() {
        Insumo insumo = Insumo.builder().id(ID).nome(NOME).precoUnitario(PRECO).build();

        InsumoJpaEntity entity = InsumoJpaMapper.toJpaEntity(insumo);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getNome()).isEqualTo(NOME);
        assertThat(entity.getPrecoUnitario()).isEqualTo(PRECO);
    }

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(InsumoJpaMapper.toJpaEntity(null)).isNull();
    }
}
