package com.fiap.mecanica.atendimento.application.port.in;

import com.fiap.mecanica.atendimento.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;

import java.util.List;

public interface TemplateUseCase {

    List<TemplateNotificacao> buscarTodos();

    TemplateNotificacao buscarPorCodigo(CodigoTemplate codigo);

    TemplateNotificacao cadastrar(TemplateNotificacao template);

    TemplateNotificacao atualizar(CodigoTemplate codigo, AtualizarTemplateCommand command);

    void deletar(CodigoTemplate codigo);
}
