package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.OrdemServicoJpaEntity;
import com.fiap.mecanica.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoSpringDataRepository extends JpaRepository<OrdemServicoJpaEntity, String> {
    List<OrdemServicoJpaEntity> findAllByStatusIn(List<Status> status);

    List<OrdemServicoJpaEntity> findAllByStatusNotIn(List<Status> status);
}
