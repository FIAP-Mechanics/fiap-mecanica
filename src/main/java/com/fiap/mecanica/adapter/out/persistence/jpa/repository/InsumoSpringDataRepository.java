package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.InsumoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsumoSpringDataRepository extends JpaRepository<InsumoJpaEntity, Long> {
}
