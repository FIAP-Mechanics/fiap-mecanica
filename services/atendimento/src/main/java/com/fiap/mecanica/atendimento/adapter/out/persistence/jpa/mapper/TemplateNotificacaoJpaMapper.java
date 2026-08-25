package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.TemplateNotificacaoJpaEntity;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;

public class TemplateNotificacaoJpaMapper {

    private TemplateNotificacaoJpaMapper() {
    }

    public static TemplateNotificacao toDomain(TemplateNotificacaoJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return TemplateNotificacao.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .conteudo(entity.getConteudo())
                .build();
    }

    public static TemplateNotificacaoJpaEntity toJpaEntity(TemplateNotificacao domain) {
        if (domain == null) {
            return null;
        }

        return TemplateNotificacaoJpaEntity.builder()
                .id(domain.getId())
                .codigo(domain.getCodigo())
                .conteudo(domain.getConteudo())
                .build();
    }
}
