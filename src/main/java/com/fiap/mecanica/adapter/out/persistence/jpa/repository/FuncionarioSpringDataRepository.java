package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionarioSpringDataRepository extends JpaRepository<FuncionarioJpaEntity, Long> {
    Optional<FuncionarioJpaEntity> findByEmail(String email);
}
