package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarTemplateCommand;
import com.fiap.mecanica.application.port.in.TemplateUseCase;
import com.fiap.mecanica.application.port.out.TemplateGateway;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.application.exception.TemplateNotFound;

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
        return templateGateway.buscarPorCodigo(codigo)
                .orElseThrow(() -> new TemplateNotFound(codigo));
    }

    @Override
    public TemplateNotificacao cadastrar(TemplateNotificacao template) {
        return templateGateway.salvar(template);
    }

    @Override
    public TemplateNotificacao atualizar(
            CodigoTemplate codigo,
            AtualizarTemplateCommand command) {
        TemplateNotificacao template = buscarPorCodigo(codigo);
        template.setConteudo(command.conteudo());
        return templateGateway.salvar(template);
    }

    @Override
    public void deletar(CodigoTemplate codigo) {
        TemplateNotificacao template = buscarPorCodigo(codigo);
        templateGateway.excluir(template);
    }
}
