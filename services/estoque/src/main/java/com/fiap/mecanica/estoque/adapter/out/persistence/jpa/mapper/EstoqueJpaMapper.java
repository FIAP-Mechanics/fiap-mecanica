package com.fiap.mecanica.estoque.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.EstoqueJpaEntity;
import com.fiap.mecanica.estoque.domain.Estoque;

public class EstoqueJpaMapper {

    private EstoqueJpaMapper() {
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

    public static EstoqueJpaEntity toJpaEntity(Estoque estoque) {
        if (estoque == null) return null;
        return EstoqueJpaEntity.builder()
                .id(estoque.getId())
                .insumo(InsumoJpaMapper.toJpaEntity(estoque.getInsumo()))
                .quantidadeInsumo(estoque.getQuantidadeInsumo())
                .ativo(estoque.isAtivo())
                .build();
    }
}
