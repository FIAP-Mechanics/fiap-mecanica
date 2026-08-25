package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.mapper.ClienteJpaMapper;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.repository.ClienteSpringDataRepository;
import com.fiap.mecanica.cliente.application.port.out.ClienteGateway;
import com.fiap.mecanica.cliente.domain.Cliente;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class ClienteJpaGateway implements ClienteGateway {

    private final ClienteSpringDataRepository repository;

    @Override
    public List<Cliente> buscarTodos() {
        return repository.findAll().stream()
                .map(ClienteJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id).map(ClienteJpaMapper::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return repository.findByDocumento(documento).map(ClienteJpaMapper::toDomain);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return repository.existsByDocumento(documento);
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        var entitySalva = repository.save(ClienteJpaMapper.toJpaEntity(cliente));
        return ClienteJpaMapper.toDomain(entitySalva);
    }
}
