package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.TemplateNotificacaoJpaEntity;
import com.fiap.mecanica.domain.TemplateNotificacao;

public final class TemplateNotificacaoJpaMapper {

    private TemplateNotificacaoJpaMapper() {
    }

    public static TemplateNotificacaoJpaEntity toJpaEntity(TemplateNotificacao template) {
        if (template == null) return null;
        return TemplateNotificacaoJpaEntity.builder()
                .id(template.getId())
                .codigo(template.getCodigo())
                .conteudo(template.getConteudo())
                .build();
    }

    public static TemplateNotificacao toDomain(TemplateNotificacaoJpaEntity entity) {
        if (entity == null) return null;
        return TemplateNotificacao.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .conteudo(entity.getConteudo())
                .build();
    }
}
