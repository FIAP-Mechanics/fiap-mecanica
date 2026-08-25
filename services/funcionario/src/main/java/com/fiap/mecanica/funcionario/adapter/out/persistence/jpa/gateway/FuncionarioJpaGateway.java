package com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.mapper.FuncionarioJpaMapper;
import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.repository.FuncionarioSpringDataRepository;
import com.fiap.mecanica.funcionario.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.funcionario.domain.Funcionario;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class FuncionarioJpaGateway implements FuncionarioGateway {

    private final FuncionarioSpringDataRepository repository;

    @Override
    public List<Funcionario> buscarTodos() {
        return repository.findAll().stream()
                .map(FuncionarioJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Funcionario> buscarPorId(Long id) {
        return repository.findById(id).map(FuncionarioJpaMapper::toDomain);
    }

    @Override
    public Optional<Funcionario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(FuncionarioJpaMapper::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Funcionario salvar(Funcionario funcionario) {
        var entitySalva = repository.save(FuncionarioJpaMapper.toJpaEntity(funcionario));
        return FuncionarioJpaMapper.toDomain(entitySalva);
    }
}
