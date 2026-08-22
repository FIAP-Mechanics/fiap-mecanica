package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoSpringDataRepository extends JpaRepository<ServicoJpaEntity, Long> {
}
