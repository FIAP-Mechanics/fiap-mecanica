package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper.FuncionarioJpaMapper;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository.FuncionarioSpringDataRepository;
import com.fiap.mecanica.atendimento.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.atendimento.domain.Funcionario;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class FuncionarioJpaGateway implements FuncionarioGateway {

    private final FuncionarioSpringDataRepository repository;

    @Override
    public Optional<Funcionario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(FuncionarioJpaMapper::toDomain);
    }

    @Override
    public Funcionario salvar(Funcionario funcionario) {
        var entitySalva = repository.save(FuncionarioJpaMapper.toJpaEntity(funcionario));
        return FuncionarioJpaMapper.toDomain(entitySalva);
    }
}
