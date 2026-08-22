package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.TemplateNotificacaoJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.TemplateSpringDataRepository;
import com.fiap.mecanica.application.port.out.TemplateGateway;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateJpaGateway implements TemplateGateway {

    private final TemplateSpringDataRepository repository;

    @Override
    public List<TemplateNotificacao> buscarTodos() {
        return repository.findAll().stream()
                .map(TemplateNotificacaoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TemplateNotificacao> buscarPorCodigo(CodigoTemplate codigo) {
        return repository.findByCodigo(codigo.name()).map(TemplateNotificacaoJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public TemplateNotificacao salvar(TemplateNotificacao template) {
        return TemplateNotificacaoJpaMapper.toDomain(
                repository.save(TemplateNotificacaoJpaMapper.toJpaEntity(template)));
    }

    @Override
    @Transactional
    public void excluir(TemplateNotificacao template) {
        repository.delete(TemplateNotificacaoJpaMapper.toJpaEntity(template));
    }
}
