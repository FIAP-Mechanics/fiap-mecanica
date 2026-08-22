package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarTemplateRequest;
import com.fiap.mecanica.adapter.in.web.request.CriarTemplateRequest;
import com.fiap.mecanica.adapter.in.web.response.TemplateDto;
import com.fiap.mecanica.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;

public final class TemplatePresenter {

    private TemplatePresenter() {
    }

    public static TemplateNotificacao toEntity(CriarTemplateRequest request) {
        return TemplateNotificacao.builder()
                .codigo(request.codigo().name())
                .conteudo(request.conteudo())
                .build();
    }

    public static AtualizarTemplateCommand toCommand(AtualizarTemplateRequest request) {
        return new AtualizarTemplateCommand(request.conteudo());
    }

    public static TemplateDto toDto(TemplateNotificacao template) {
        return TemplateDto.builder()
                .codigo(CodigoTemplate.valueOf(template.getCodigo()))
                .conteudo(template.getConteudo())
                .build();
    }
}
