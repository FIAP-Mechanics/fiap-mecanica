package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import com.fiap.mecanica.domain.Servico;

public final class ServicoJpaMapper {

    private ServicoJpaMapper() {
    }

    public static ServicoJpaEntity toJpaEntity(Servico servico) {
        if (servico == null) return null;
        return ServicoJpaEntity.builder()
                .id(servico.getId())
                .nome(servico.getNome())
                .descricao(servico.getDescricao())
                .valor(servico.getValor())
                .ativo(servico.isAtivo())
                .build();
    }

    public static Servico toDomain(ServicoJpaEntity entity) {
        if (entity == null) return null;
        return Servico.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .valor(entity.getValor())
                .ativo(entity.isAtivo())
                .build();
    }
}
