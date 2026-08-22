package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.ServicoJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.ServicoSpringDataRepository;
import com.fiap.mecanica.application.port.out.ServicoGateway;
import com.fiap.mecanica.domain.Servico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicoJpaGateway implements ServicoGateway {

    private final ServicoSpringDataRepository repository;

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        return repository.findById(id).map(ServicoJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Servico salvar(Servico servico) {
        return ServicoJpaMapper.toDomain(repository.save(ServicoJpaMapper.toJpaEntity(servico)));
    }
}
