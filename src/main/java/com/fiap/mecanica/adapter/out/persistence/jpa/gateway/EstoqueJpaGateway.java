package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.EstoqueJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.EstoqueSpringDataRepository;
import com.fiap.mecanica.application.port.out.EstoqueGateway;
import com.fiap.mecanica.domain.Estoque;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstoqueJpaGateway implements EstoqueGateway {

    private final EstoqueSpringDataRepository repository;

    @Override
    public List<Estoque> buscarAtivos() {
        return repository.findAllByAtivoTrue().stream()
                .map(EstoqueJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Estoque> buscarPorInsumoId(Long idInsumo) {
        return repository.findByInsumoId(idInsumo).map(EstoqueJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Estoque salvar(Estoque estoque) {
        return EstoqueJpaMapper.toDomain(repository.save(EstoqueJpaMapper.toJpaEntity(estoque)));
    }
}
