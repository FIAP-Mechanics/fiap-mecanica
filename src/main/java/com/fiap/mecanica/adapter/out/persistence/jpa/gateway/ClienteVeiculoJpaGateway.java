package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.VeiculoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.ClienteVeiculoJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.ClienteVeiculoSpringDataRepository;
import com.fiap.mecanica.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.domain.ClienteVeiculo;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteVeiculoJpaGateway implements ClienteVeiculoGateway {

    private final ClienteVeiculoSpringDataRepository repository;
    private final EntityManager entityManager;

    @Override
    public boolean existeVinculo(Long clienteId, Long veiculoId) {
        return repository.existsByClienteIdAndVeiculoId(clienteId, veiculoId);
    }

    @Override
    public List<ClienteVeiculo> buscarPorClienteId(Long clienteId) {
        return repository.findByClienteId(clienteId).stream()
                .map(ClienteVeiculoJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public ClienteVeiculo salvar(ClienteVeiculo vinculo) {
        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaMapper.toJpaEntity(vinculo);
        if (entity.getCliente() != null && entity.getCliente().getId() != null) {
            entity.setCliente(entityManager.getReference(ClienteJpaEntity.class, entity.getCliente().getId()));
        }
        if (entity.getVeiculo() != null && entity.getVeiculo().getId() != null) {
            entity.setVeiculo(entityManager.getReference(VeiculoJpaEntity.class, entity.getVeiculo().getId()));
        }
        return ClienteVeiculoJpaMapper.toDomain(repository.save(entity));
    }
}
