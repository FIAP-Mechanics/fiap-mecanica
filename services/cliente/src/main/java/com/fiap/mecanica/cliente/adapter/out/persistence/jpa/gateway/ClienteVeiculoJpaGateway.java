package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.mapper.ClienteVeiculoJpaMapper;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.repository.ClienteVeiculoSpringDataRepository;
import com.fiap.mecanica.cliente.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Component
@Transactional
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
    public ClienteVeiculo salvar(ClienteVeiculo vinculo) {
        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaMapper.toJpaEntity(vinculo);
        if (vinculo.getCliente() != null && vinculo.getCliente().getId() != null) {
            entity.setCliente(entityManager.getReference(ClienteJpaEntity.class, vinculo.getCliente().getId()));
        }
        return ClienteVeiculoJpaMapper.toDomain(repository.save(entity));
    }
}
