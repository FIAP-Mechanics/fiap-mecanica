package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteSpringDataRepository extends JpaRepository<ClienteJpaEntity, Long> {
    boolean existsByDocumento(String documento);

    Optional<ClienteJpaEntity> findByDocumento(String documento);
}
