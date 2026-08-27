package com.fiap.mecanica.atendimento.application.usecase;

import com.fiap.mecanica.atendimento.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.atendimento.application.port.in.TemplateUseCase;
import com.fiap.mecanica.atendimento.application.port.out.TemplateGateway;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;

import java.util.List;

public class TemplateInteractor implements TemplateUseCase {

    private final TemplateGateway templateGateway;

    public TemplateInteractor(TemplateGateway templateGateway) {
        this.templateGateway = templateGateway;
    }

    @Override
    public List<TemplateNotificacao> buscarTodos() {
        return templateGateway.buscarTodos();
    }

    @Override
    public TemplateNotificacao buscarPorCodigo(CodigoTemplate codigo) {
        return templateGateway.buscarPorCodigo(codigo.name()).orElseThrow(() -> new TemplateNotFound(codigo));
    }

    @Override
    public TemplateNotificacao cadastrar(TemplateNotificacao template) {
        return templateGateway.salvar(template);
    }

    @Override
    public TemplateNotificacao atualizar(CodigoTemplate codigo, AtualizarTemplateCommand command) {
        TemplateNotificacao template = this.buscarPorCodigo(codigo);
        template.setConteudo(command.conteudo());
        return templateGateway.salvar(template);
    }

    @Override
    public void deletar(CodigoTemplate codigo) {
        TemplateNotificacao template = this.buscarPorCodigo(codigo);
        templateGateway.deletar(template);
    }
}
