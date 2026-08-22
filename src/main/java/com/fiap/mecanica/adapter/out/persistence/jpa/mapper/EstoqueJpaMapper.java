package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.EstoqueJpaEntity;
import com.fiap.mecanica.domain.Estoque;

public final class EstoqueJpaMapper {

    private EstoqueJpaMapper() {
    }

    public static EstoqueJpaEntity toJpaEntity(Estoque estoque) {
        if (estoque == null) return null;
        return EstoqueJpaEntity.builder()
                .id(estoque.getId())
                .insumo(InsumoJpaMapper.toJpaEntity(estoque.getInsumo()))
                .quantidadeInsumo(estoque.getQuantidadeInsumo())
                .ativo(estoque.isAtivo())
                .build();
    }

    public static Estoque toDomain(EstoqueJpaEntity entity) {
        if (entity == null) return null;
        return Estoque.builder()
                .id(entity.getId())
                .insumo(InsumoJpaMapper.toDomain(entity.getInsumo()))
                .quantidadeInsumo(entity.getQuantidadeInsumo())
                .ativo(entity.isAtivo())
                .build();
    }
}
