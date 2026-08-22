package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.ClienteJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.ClienteSpringDataRepository;
import com.fiap.mecanica.application.port.out.ClienteGateway;
import com.fiap.mecanica.domain.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    @Transactional
    public Cliente salvar(Cliente cliente) {
        return ClienteJpaMapper.toDomain(repository.save(ClienteJpaMapper.toJpaEntity(cliente)));
    }
}
