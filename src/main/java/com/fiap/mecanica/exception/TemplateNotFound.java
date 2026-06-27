package com.fiap.mecanica.exception;

import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;

public class TemplateNotFound extends EntidadeNaoEncontradaException {

    public TemplateNotFound(CodigoTemplate codigo) {
        super("Template de notificação não encontrado: " + codigo.name());
    }
}
