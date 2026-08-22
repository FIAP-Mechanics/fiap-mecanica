package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.FuncionarioJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.FuncionarioSpringDataRepository;
import com.fiap.mecanica.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.domain.Funcionario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuncionarioJpaGateway implements FuncionarioGateway {

    private final FuncionarioSpringDataRepository repository;

    @Override
    public Optional<Funcionario> buscarPorId(Long id) {
        return repository.findById(id).map(FuncionarioJpaMapper::toDomain);
    }

    @Override
    public Optional<Funcionario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(FuncionarioJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Funcionario salvar(Funcionario funcionario) {
        return FuncionarioJpaMapper.toDomain(repository.save(FuncionarioJpaMapper.toJpaEntity(funcionario)));
    }
}
