package com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import com.fiap.mecanica.funcionario.domain.Funcionario;

public class FuncionarioJpaMapper {

    private FuncionarioJpaMapper() {
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
}
