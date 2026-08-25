package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;

public class ClienteVeiculoJpaMapper {

    private ClienteVeiculoJpaMapper() {
    }

    public static ClienteVeiculo toDomain(ClienteVeiculoJpaEntity entity) {
        if (entity == null) return null;
        return ClienteVeiculo.builder()
                .id(entity.getId())
                .cliente(ClienteJpaMapper.toDomain(entity.getCliente()))
                .veiculoId(entity.getVeiculoId())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .ano(entity.getAno())
                .build();
    }

    public static ClienteVeiculoJpaEntity toJpaEntity(ClienteVeiculo vinculo) {
        if (vinculo == null) return null;
        return ClienteVeiculoJpaEntity.builder()
                .id(vinculo.getId())
                .cliente(ClienteJpaMapper.toJpaEntity(vinculo.getCliente()))
                .veiculoId(vinculo.getVeiculoId())
                .placa(vinculo.getPlaca())
                .marca(vinculo.getMarca())
                .modelo(vinculo.getModelo())
                .ano(vinculo.getAno())
                .build();
    }
}
