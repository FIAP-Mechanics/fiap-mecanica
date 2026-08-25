package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.repository;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteVeiculoSpringDataRepository extends JpaRepository<ClienteVeiculoJpaEntity, Long> {
    boolean existsByClienteIdAndVeiculoId(Long clienteId, Long veiculoId);

    List<ClienteVeiculoJpaEntity> findByClienteId(Long clienteId);
}
