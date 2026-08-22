package com.fiap.mecanica.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.InsumoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.OrdemServicoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.VeiculoJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.mapper.OrdemServicoJpaMapper;
import com.fiap.mecanica.adapter.out.persistence.jpa.repository.OrdemServicoSpringDataRepository;
import com.fiap.mecanica.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.Status;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdemServicoJpaGateway implements OrdemServicoGateway {

    private final OrdemServicoSpringDataRepository repository;
    private final EntityManager entityManager;

    @Override
    public Optional<OrdemServico> buscarPorId(String id) {
        return repository.findById(id).map(OrdemServicoJpaMapper::toDomain);
    }

    @Override
    public List<OrdemServico> buscarPorStatusEm(List<Status> status) {
        return repository.findAllByStatusIn(status).stream()
                .map(OrdemServicoJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<OrdemServico> buscarPorStatusForaDe(List<Status> status) {
        return repository.findAllByStatusNotIn(status).stream()
                .map(OrdemServicoJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public OrdemServico salvar(OrdemServico ordemServico) {
        OrdemServicoJpaEntity entity = OrdemServicoJpaMapper.toJpaEntity(ordemServico);
        attachExistingReferences(entity);
        return OrdemServicoJpaMapper.toDomain(repository.save(entity));
    }

    private void attachExistingReferences(OrdemServicoJpaEntity entity) {
        if (entity.getCliente() != null && entity.getCliente().getId() != null) {
            entity.setCliente(entityManager.getReference(ClienteJpaEntity.class, entity.getCliente().getId()));
        }
        if (entity.getVeiculo() != null && entity.getVeiculo().getId() != null) {
            entity.setVeiculo(entityManager.getReference(VeiculoJpaEntity.class, entity.getVeiculo().getId()));
        }
        if (entity.getOrcamento() == null) {
            return;
        }
        if (entity.getOrcamento().getServicos() != null) {
            entity.getOrcamento().getServicos().forEach(item -> {
                if (item.getServico() != null && item.getServico().getId() != null) {
                    item.setServico(entityManager.getReference(ServicoJpaEntity.class, item.getServico().getId()));
                }
            });
        }
        if (entity.getOrcamento().getInsumos() != null) {
            entity.getOrcamento().getInsumos().forEach(item -> {
                if (item.getInsumo() != null && item.getInsumo().getId() != null) {
                    item.setInsumo(entityManager.getReference(InsumoJpaEntity.class, item.getInsumo().getId()));
                }
            });
        }
    }
}
