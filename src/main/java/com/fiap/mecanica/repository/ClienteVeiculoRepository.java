package com.fiap.mecanica.repository;

import com.fiap.mecanica.domain.ClienteVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteVeiculoRepository extends JpaRepository<ClienteVeiculo, Long> {
    boolean existsByClienteIdAndVeiculoId(Long clienteId, Long veiculoId);
    List<ClienteVeiculo> findByClienteId(Long clienteId);
}
