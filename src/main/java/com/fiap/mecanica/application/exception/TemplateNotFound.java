package com.fiap.mecanica.application.exception;

import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.exception.EntidadeNaoEncontradaException;

public class TemplateNotFound extends EntidadeNaoEncontradaException {

    public TemplateNotFound(CodigoTemplate codigo) {
        super("Template de notificação não encontrado: " + codigo.name());
    }
}
