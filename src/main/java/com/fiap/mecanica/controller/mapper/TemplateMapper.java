package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.CriarTemplateRequest;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.dto.TemplateDto;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;

public class TemplateMapper {

    private TemplateMapper() {}

    public static TemplateNotificacao toEntity(CriarTemplateRequest request) {
        return TemplateNotificacao.builder()
                .codigo(request.codigo().name())
                .conteudo(request.conteudo())
                .build();
    }

    public static TemplateDto toDto(TemplateNotificacao template) {
        return TemplateDto.builder()
                .codigo(CodigoTemplate.valueOf(template.getCodigo()))
                .conteudo(template.getConteudo())
                .build();
    }
}
