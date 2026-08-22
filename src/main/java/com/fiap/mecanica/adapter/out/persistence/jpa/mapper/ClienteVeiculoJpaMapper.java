package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import com.fiap.mecanica.domain.ClienteVeiculo;

public final class ClienteVeiculoJpaMapper {

    private ClienteVeiculoJpaMapper() {
    }

    public static ClienteVeiculoJpaEntity toJpaEntity(ClienteVeiculo vinculo) {
        if (vinculo == null) return null;
        return ClienteVeiculoJpaEntity.builder()
                .id(vinculo.getId())
                .cliente(ClienteJpaMapper.toJpaEntity(vinculo.getCliente()))
                .veiculo(VeiculoJpaMapper.toJpaEntity(vinculo.getVeiculo()))
                .build();
    }

    public static ClienteVeiculo toDomain(ClienteVeiculoJpaEntity entity) {
        if (entity == null) return null;
        return ClienteVeiculo.builder()
                .id(entity.getId())
                .cliente(ClienteJpaMapper.toDomain(entity.getCliente()))
                .veiculo(VeiculoJpaMapper.toDomain(entity.getVeiculo()))
                .build();
    }
}
