package com.fiap.mecanica.cliente.repository;

import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteVeiculoRepository extends JpaRepository<ClienteVeiculo, Long> {
    boolean existsByClienteIdAndVeiculoId(Long clienteId, Long veiculoId);

    List<ClienteVeiculo> findByClienteId(Long clienteId);
}
