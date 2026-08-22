package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.InsumoJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.InsumoSpringDataRepository;
import com.fiap.mecanica.application.port.out.InsumoGateway;
import com.fiap.mecanica.domain.Insumo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsumoJpaGateway implements InsumoGateway {

    private final InsumoSpringDataRepository repository;

    @Override
    public Optional<Insumo> buscarPorId(Long id) {
        return repository.findById(id).map(InsumoJpaMapper::toDomain);
    }
}
