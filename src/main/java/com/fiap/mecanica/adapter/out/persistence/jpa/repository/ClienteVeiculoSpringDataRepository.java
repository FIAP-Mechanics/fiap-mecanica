package com.fiap.mecanica.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteVeiculoSpringDataRepository extends JpaRepository<ClienteVeiculoJpaEntity, Long> {
    boolean existsByClienteIdAndVeiculoId(Long clienteId, Long veiculoId);

    List<ClienteVeiculoJpaEntity> findByClienteId(Long clienteId);
}
