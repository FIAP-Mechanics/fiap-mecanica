package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.TemplateNotificacaoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper.TemplateNotificacaoJpaMapper;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository.TemplateSpringDataRepository;
import com.fiap.mecanica.atendimento.application.port.out.TemplateGateway;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class TemplateJpaGateway implements TemplateGateway {

    private final TemplateSpringDataRepository repository;

    @Override
    public List<TemplateNotificacao> buscarTodos() {
        return repository.findAll().stream()
                .map(TemplateNotificacaoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TemplateNotificacao> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(TemplateNotificacaoJpaMapper::toDomain);
    }

    @Override
    public TemplateNotificacao salvar(TemplateNotificacao template) {
        var entitySalva = repository.save(TemplateNotificacaoJpaMapper.toJpaEntity(template));
        return TemplateNotificacaoJpaMapper.toDomain(entitySalva);
    }

    @Override
    public void deletar(TemplateNotificacao template) {
        TemplateNotificacaoJpaEntity entity = TemplateNotificacaoJpaMapper.toJpaEntity(template);
        repository.delete(entity);
    }
}
