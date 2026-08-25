package com.fiap.mecanica.atendimento.application.port.out;

import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;

import java.util.List;
import java.util.Optional;

public interface TemplateGateway {

    List<TemplateNotificacao> buscarTodos();

    Optional<TemplateNotificacao> buscarPorCodigo(String codigo);

    TemplateNotificacao salvar(TemplateNotificacao template);

    void deletar(TemplateNotificacao template);
}
