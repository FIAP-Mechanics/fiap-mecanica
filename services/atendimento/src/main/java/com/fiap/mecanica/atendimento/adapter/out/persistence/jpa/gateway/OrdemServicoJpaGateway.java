package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.mapper.OrdemServicoJpaMapper;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository.OrdemServicoSpringDataRepository;
import com.fiap.mecanica.atendimento.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.Status;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
@Transactional
public class OrdemServicoJpaGateway implements OrdemServicoGateway {

    private final OrdemServicoSpringDataRepository repository;

    @Override
    public OrdemServico salvar(OrdemServico ordemServico) {
        var entitySalva = repository.save(OrdemServicoJpaMapper.toJpaEntity(ordemServico));
        return OrdemServicoJpaMapper.toDomain(entitySalva);
    }

    @Override
    public Optional<OrdemServico> buscarPorId(String id) {
        return repository.findById(id).map(OrdemServicoJpaMapper::toDomain);
    }

    @Override
    public List<OrdemServico> buscarTodosPorStatusNotIn(List<Status> status) {
        return repository.findAllByStatusNotIn(status).stream()
                .map(OrdemServicoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<OrdemServico> buscarTodosPorStatusIn(List<Status> status) {
        return repository.findAllByStatusIn(status).stream()
                .map(OrdemServicoJpaMapper::toDomain)
                .toList();
    }
}
