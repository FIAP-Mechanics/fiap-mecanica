package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.dto.TemplateDto;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TemplateService {

    private final TemplateRepository repository;

    public List<TemplateNotificacao> buscarTodos() {
        return repository.findAll();
    }

    public TemplateNotificacao buscarPorCodigo(CodigoTemplate codigo) {
        return repository.findByCodigo(codigo.name()).orElseThrow(() -> new TemplateNotFound(codigo));
    }

    public TemplateNotificacao cadastrar(TemplateNotificacao template) {
        return repository.save(template);
    }

    public TemplateNotificacao atualizar(CodigoTemplate codigo, TemplateDto dto) {
        TemplateNotificacao template = this.buscarPorCodigo(codigo);
        template.setConteudo(dto.conteudo());
        return repository.save(template);
    }

    public void deletar(CodigoTemplate codigo) {
        TemplateNotificacao template = this.buscarPorCodigo(codigo);
        repository.delete(template);
    }
}
