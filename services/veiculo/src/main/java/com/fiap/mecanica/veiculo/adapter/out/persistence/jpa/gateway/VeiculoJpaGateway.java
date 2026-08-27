package com.fiap.mecanica.veiculo.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.veiculo.adapter.out.persistence.jpa.mapper.VeiculoJpaMapper;
import com.fiap.mecanica.veiculo.adapter.out.persistence.jpa.repository.VeiculoSpringDataRepository;
import com.fiap.mecanica.veiculo.application.port.out.VeiculoGateway;
import com.fiap.mecanica.veiculo.domain.Veiculo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class VeiculoJpaGateway implements VeiculoGateway {

    private final VeiculoSpringDataRepository repository;

    @Override
    public List<Veiculo> buscarTodos() {
        return repository.findAll().stream()
                .map(VeiculoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return repository.findById(id).map(VeiculoJpaMapper::toDomain);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(String placa) {
        return repository.findByPlaca(placa).map(VeiculoJpaMapper::toDomain);
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return repository.existsByPlaca(placa);
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        var entitySalva = repository.save(VeiculoJpaMapper.toJpaEntity(veiculo));
        return VeiculoJpaMapper.toDomain(entitySalva);
    }
}
