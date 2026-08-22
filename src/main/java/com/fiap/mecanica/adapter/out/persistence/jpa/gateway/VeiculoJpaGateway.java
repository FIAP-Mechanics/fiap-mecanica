package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.VeiculoJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.VeiculoSpringDataRepository;
import com.fiap.mecanica.application.port.out.VeiculoGateway;
import com.fiap.mecanica.domain.Veiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeiculoJpaGateway implements VeiculoGateway {

    private final VeiculoSpringDataRepository repository;

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return repository.findById(id).map(VeiculoJpaMapper::toDomain);
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return repository.existsByPlaca(placa);
    }

    @Override
    @Transactional
    public Veiculo salvar(Veiculo veiculo) {
        return VeiculoJpaMapper.toDomain(repository.save(VeiculoJpaMapper.toJpaEntity(veiculo)));
    }
}
