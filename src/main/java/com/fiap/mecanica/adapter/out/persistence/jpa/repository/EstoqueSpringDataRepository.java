package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.EstoqueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueSpringDataRepository extends JpaRepository<EstoqueJpaEntity, Long> {
    List<EstoqueJpaEntity> findAllByAtivoTrue();

    Optional<EstoqueJpaEntity> findByInsumoId(Long idInsumo);
}
