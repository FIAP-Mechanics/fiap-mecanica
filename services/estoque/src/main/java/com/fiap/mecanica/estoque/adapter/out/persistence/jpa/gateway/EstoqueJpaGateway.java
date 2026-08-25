package com.fiap.mecanica.estoque.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.mapper.EstoqueJpaMapper;
import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.repository.EstoqueSpringDataRepository;
import com.fiap.mecanica.estoque.application.port.out.EstoqueGateway;
import com.fiap.mecanica.estoque.domain.Estoque;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class EstoqueJpaGateway implements EstoqueGateway {

    private final EstoqueSpringDataRepository repository;

    @Override
    public List<Estoque> buscarTodosAtivos() {
        return repository.findAllByAtivoTrue().stream()
                .map(EstoqueJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Estoque> buscarPorIdInsumo(Long idInsumo) {
        return repository.findByInsumoId(idInsumo).map(EstoqueJpaMapper::toDomain);
    }

    @Override
    public Estoque salvar(Estoque estoque) {
        var entitySalva = repository.save(EstoqueJpaMapper.toJpaEntity(estoque));
        return EstoqueJpaMapper.toDomain(entitySalva);
    }

    @Override
    public List<Estoque> salvarTodos(List<Estoque> estoques) {
        var entidades = estoques.stream().map(EstoqueJpaMapper::toJpaEntity).toList();
        return repository.saveAll(entidades).stream()
                .map(EstoqueJpaMapper::toDomain)
                .toList();
    }
}
