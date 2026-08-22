package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.VeiculoJpaEntity;
import com.fiap.mecanica.domain.Veiculo;

public final class VeiculoJpaMapper {

    private VeiculoJpaMapper() {
    }

    public static VeiculoJpaEntity toJpaEntity(Veiculo veiculo) {
        if (veiculo == null) return null;
        return VeiculoJpaEntity.builder()
                .id(veiculo.getId())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .placa(veiculo.getPlaca())
                .ano(veiculo.getAno())
                .ativo(veiculo.isAtivo())
                .build();
    }

    public static Veiculo toDomain(VeiculoJpaEntity entity) {
        if (entity == null) return null;
        return Veiculo.builder()
                .id(entity.getId())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .placa(entity.getPlaca())
                .ano(entity.getAno())
                .ativo(entity.isAtivo())
                .build();
    }
}
