package com.fiap.mecanica.application.port.out;

import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;

import java.util.List;
import java.util.Optional;

public interface TemplateGateway {
    List<TemplateNotificacao> buscarTodos();
    Optional<TemplateNotificacao> buscarPorCodigo(CodigoTemplate codigo);
    TemplateNotificacao salvar(TemplateNotificacao template);
    void excluir(TemplateNotificacao template);
}
