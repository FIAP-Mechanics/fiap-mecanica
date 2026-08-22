package com.fiap.mecanica.atendimento.exception;

import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;

public class TemplateNotFound extends EntidadeNaoEncontradaException {

    public TemplateNotFound(CodigoTemplate codigo) {
        super("Template de notificação não encontrado: " + codigo.name());
    }
}
