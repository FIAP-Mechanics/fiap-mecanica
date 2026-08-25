package com.fiap.mecanica.servico.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.servico.adapter.out.persistence.jpa.mapper.ServicoJpaMapper;
import com.fiap.mecanica.servico.adapter.out.persistence.jpa.repository.ServicoSpringDataRepository;
import com.fiap.mecanica.servico.application.port.out.ServicoGateway;
import com.fiap.mecanica.servico.domain.Servico;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class ServicoJpaGateway implements ServicoGateway {

    private final ServicoSpringDataRepository repository;

    @Override
    public List<Servico> buscarTodos() {
        return repository.findAll().stream()
                .map(ServicoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        return repository.findById(id).map(ServicoJpaMapper::toDomain);
    }

    @Override
    public Servico salvar(Servico servico) {
        var entitySalva = repository.save(ServicoJpaMapper.toJpaEntity(servico));
        return ServicoJpaMapper.toDomain(entitySalva);
    }
}
