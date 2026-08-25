package com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.funcionario.adapter.out.persistence.jpa.entity.FuncionarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioSpringDataRepository extends JpaRepository<FuncionarioJpaEntity, Long> {
    Optional<FuncionarioJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
