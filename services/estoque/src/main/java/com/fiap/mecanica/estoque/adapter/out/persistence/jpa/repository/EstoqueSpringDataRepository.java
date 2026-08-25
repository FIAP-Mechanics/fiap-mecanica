package com.fiap.mecanica.estoque.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.estoque.adapter.out.persistence.jpa.entity.EstoqueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueSpringDataRepository extends JpaRepository<EstoqueJpaEntity, Long> {
    List<EstoqueJpaEntity> findAllByAtivoTrue();

    Optional<EstoqueJpaEntity> findByInsumoId(Long idInsumo);
}
