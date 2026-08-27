package com.fiap.mecanica.atendimento.adapter.in.web.presenter;

import com.fiap.mecanica.atendimento.adapter.in.web.request.CriarTemplateRequest;
import com.fiap.mecanica.atendimento.adapter.in.web.response.TemplateDto;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;

public class TemplatePresenter {

    private TemplatePresenter() {
    }

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
