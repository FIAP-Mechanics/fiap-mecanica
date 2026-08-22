package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.InsumoJpaEntity;
import com.fiap.mecanica.domain.Insumo;

public final class InsumoJpaMapper {

    private InsumoJpaMapper() {
    }

    public static InsumoJpaEntity toJpaEntity(Insumo insumo) {
        if (insumo == null) return null;
        return InsumoJpaEntity.builder()
                .id(insumo.getId())
                .nome(insumo.getNome())
                .precoUnitario(insumo.getPrecoUnitario())
                .build();
    }

    public static Insumo toDomain(InsumoJpaEntity entity) {
        if (entity == null) return null;
        return Insumo.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .precoUnitario(entity.getPrecoUnitario())
                .build();
    }
}
