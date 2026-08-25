package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import com.fiap.mecanica.atendimento.domain.Funcionario;

public class FuncionarioJpaMapper {

    private FuncionarioJpaMapper() {
    }

    public static Funcionario toDomain(FuncionarioJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Funcionario.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .senha(entity.getSenha())
                .nome(entity.getNome())
                .funcao(entity.getFuncao())
                .ativo(entity.isAtivo())
                .build();
    }

    public static FuncionarioJpaEntity toJpaEntity(Funcionario domain) {
        if (domain == null) {
            return null;
        }

        return FuncionarioJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .senha(domain.getSenha())
                .nome(domain.getNome())
                .funcao(domain.getFuncao())
                .ativo(domain.isAtivo())
                .build();
    }
}
