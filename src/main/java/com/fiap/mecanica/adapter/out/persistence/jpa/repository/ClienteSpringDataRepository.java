package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteSpringDataRepository extends JpaRepository<ClienteJpaEntity, Long> {
    Optional<ClienteJpaEntity> findByDocumento(String documento);

    boolean existsByDocumento(String documento);
}
