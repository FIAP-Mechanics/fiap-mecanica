package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import com.fiap.mecanica.domain.Funcionario;

public final class FuncionarioJpaMapper {

    private FuncionarioJpaMapper() {
    }

    public static FuncionarioJpaEntity toJpaEntity(Funcionario funcionario) {
        if (funcionario == null) return null;
        return FuncionarioJpaEntity.builder()
                .id(funcionario.getId())
                .email(funcionario.getEmail())
                .senha(funcionario.getSenha())
                .nome(funcionario.getNome())
                .funcao(funcionario.getFuncao())
                .ativo(funcionario.isAtivo())
                .build();
    }

    public static Funcionario toDomain(FuncionarioJpaEntity entity) {
        if (entity == null) return null;
        return Funcionario.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .senha(entity.getSenha())
                .nome(entity.getNome())
                .funcao(entity.getFuncao())
                .ativo(entity.isAtivo())
                .build();
    }
}
